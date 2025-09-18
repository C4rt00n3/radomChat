package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.AppRoutes
import com.example.meettalk.data.local.model.RealmClass.*
import com.example.meettalk.data.local.model.body.CreateLocation
import com.example.meettalk.data.local.model.body.UpdateUser
import com.example.meettalk.data.local.model.entities.*
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.data.remote.UserRequests
import com.example.meettalk.data.remote.UsersEndpoints
import com.example.meettalk.presentation.components.UiState
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TaskManager
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.getAddressFromLocation
import com.example.meettalk.utils.getSubFromJwt
import com.example.meettalk.utils.isJwtExpired
import com.example.meettalk.utils.showToast
import com.example.meettalk.utils.users.updateUserInRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * ViewModel responsável por gerenciar a lógica de negócios e o estado relacionado aos usuários.
 * Lida com autenticação, perfis de usuário, upload de imagens, localização e bloqueios.
 */
class UserViewModel : ViewModel() {

    // --- Variáveis de Estado Públicas ---

    private val _myUser = MutableStateFlow<User?>(null)
    /** Representa o usuário logado, exposto como um StateFlow. */
    val myUser: StateFlow<User?> = _myUser.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _uiState = MutableStateFlow<UiState>(UiState.Success())
    @RequiresApi(Build.VERSION_CODES.O)
            /** Representa o estado atual da UI, como Loading, Success ou Error. */
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _token = MutableStateFlow("")
    /** Contém o token de autenticação atual do usuário. */
    val token: StateFlow<String> = _token.asStateFlow()

    private val _users = MutableStateFlow(emptyList<User>())
    /** Representa uma lista de usuários, utilizada para fins como a busca de usuários aleatórios. */
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _blocks = MutableStateFlow(emptyList<Block>())
    /** Representa a lista de usuários bloqueados pelo usuário atual. */
    val blocks: StateFlow<List<Block>> = _blocks.asStateFlow()

    // --- Dependências e Ferramentas Privadas ---

    private val formatR = FormatClass()
    private val formatRealm = FormatRealm()
    private val taskManager = TaskManager()

    private lateinit var realm: Realm
    private lateinit var context: Context
    private lateinit var tokenManager: TokenManager
    private lateinit var apiServiceChat: ChatEndPoint
    private lateinit var apiUserService: UsersEndpoints
    private lateinit var chatRequests: ChatRequests
    private lateinit var userRequests: UserRequests
    private lateinit var baseUrl: String

    // --- Métodos Públicos ---

    /**
     * Constrói e inicializa as dependências do ViewModel.
     * Esta função deve ser chamada uma única vez quando o ViewModel é criado.
     *
     * @param context Contexto da aplicação, necessário para acessar recursos.
     * @param realm Instância opcional do Realm. Se nula, uma nova instância será aberta.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(context: Context, realm: Realm? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                initializeDependencies(context, realm)
                loadUserAndTokenFromStorage()
                _myUser.value?.let { _uiState.value = UiState.Success(it) }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Falha na inicialização: ${e.localizedMessage}")
                Log.e("UserViewModel", "Erro na inicialização: ${e.message}", e)
            }
        }
    }


    /**
     * Valida o token de autenticação e tenta renová-lo se estiver expirado.
     * Se o servidor estiver inacessível, tenta carregar o usuário e os chats do cache local.
     * @return A rota de navegação a ser seguida (`CHAT_LIST` ou `LOGIN`).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun handleTokenValidation(): String {
        val localToken = tokenManager.getToken()
        if (!localToken.isNullOrBlank() && !isJwtExpired(localToken.replace("Bearer ", ""))) {
            // Token local é válido e não expirou.
            // O usuário pode acessar o conteúdo offline.
            return AppRoutes.CHAT_LIST
        }

        // 2. Tenta renovar o token com o servidor.
        return try {
            val newToken = chatRequests.refreshAndGetToken(tokenManager.getRefreshToken() ?: "")
            if (newToken != null) {
                tokenManager.saveToken(newToken.accessToken)
                tokenManager.saveRefreshToken(newToken.refreshToken)
                tokenManager.saveUser(newToken.user)
                return AppRoutes.CHAT_LIST
            } else {
                // Se a API não retornou um token, tenta o cache local.
                checkLocalCache()
            }
        } catch (e: Exception) {
            // 3. Em caso de falha na conexão (internet ou servidor),
            // verifica se há dados locais para exibir.
            Log.e("AuthError", "Falha na renovação do token, verificando cache local", e)
            checkLocalCache()
        }
    }

    /**
     * Verifica se há um usuário no cache local do Realm para permitir o acesso offline.
     * @return A rota de navegação (`CHAT_LIST` se houver usuário, `LOGIN` caso contrário).
     */
    private fun checkLocalCache(): String {
        val userUuid = getSubFromJwt(tokenManager.getToken() ?: "")
        val hasLocalUser = realm.query<UserRealm>("uuid == $0", userUuid).find().firstOrNull() != null

        return if (hasLocalUser) {
            Log.d("OfflineAccess", "Acesso offline concedido. Usuário encontrado no Realm.")
            AppRoutes.CHAT_LIST
        } else {
            AppRoutes.LOGIN
        }
    }

    /**
     * Encontra um usuário no Realm pelo UUID.
     * @param uuid O UUID do usuário a ser encontrado.
     * @return O objeto User correspondente ou null.
     */
    fun findUser(uuid: String): User? {
        return realm.query<UserRealm>("uuid == $0", uuid).first().find()?.let {
            formatR.fromUserRealm(it)
        }
    }

    /**
     * Busca um usuário aleatório da API remota.
     * @return Retorna um objeto [User] ou `null` em caso de erro.
     */
    suspend fun randomUser(): User? {
        return userRequests.random(_token.value)
    }

    /**
     * Define a lista de usuários no StateFlow.
     * @param list A lista de usuários.
     */
    fun setUsers(list: List<User>) {
        _users.value = list
    }

    /**
     * Busca um usuário pelo UUID, primeiro no cache local e depois na API, se necessário.
     * @param uuid O UUID do usuário.
     * @param requestOn Força a busca na API.
     * @return O objeto [User] ou null.
     */
    suspend fun getUser(uuid: String, requestOn: Boolean = false): User? {
        if (!requestOn) {
            val cachedUser = users.value.find { it.uuid == uuid }
                ?: realm.query<UserRealm>("uuid == $0", uuid).first().find()?.let { formatR.fromUserRealm(it) }
            return cachedUser ?: userRequests.getUser(uuid, _token.value)
        }
        return userRequests.getUser(uuid, _token.value)
    }

    /**
     * Lida com o upload de uma imagem de perfil.
     * @param uri O URI da imagem selecionada.
     * @param uuid UUID da imagem (opcional, para atualização).
     * @param slot Posição da imagem no perfil.
     */
    fun uploadProfileImage(uri: Uri, uuid: String? = null, slot: Int = 0) {
        viewModelScope.launch {
            handleImageUpload(uri, uuid, slot)
        }
    }

    /**
     * Atualiza as informações de um usuário no servidor e no banco de dados local.
     * @param user O objeto [User] com as informações atualizadas.
     * @param location O objeto [CreateLocation] com as novas coordenadas.
     * @param onFinally Lambda a ser executada ao final da operação.
     */
    fun updateUser(user: User, location: CreateLocation? = null, onFinally: () -> Unit) {
        taskManager.addTask {
            handleUserUpdate(user, location, onFinally)
        }
    }

    /**
     * Insere a localização geográfica do usuário, geocodificando as coordenadas.
     * @param pair Um [Pair] com latitude e longitude.
     */
    fun insertLocation(pair: Pair<Double, Double>) {
        val currentUser = _myUser.value ?: run {
            Log.e("LocationUpdate", "Usuário atual inválido. Abortando.")
            return
        }

        val userId = currentUser.uuid
        val existingLocation = realm.query<LocationRealm>("userId == $0", userId).first().find()

        val shouldUpdate = existingLocation == null || isLocationOutdated(existingLocation.updatedAt)

        if (shouldUpdate) {
            Log.d("LocationUpdate", "Iniciando atualização de localização para o usuário: $userId")
            getAddressFromLocation(
                context,
                pair.first,
                pair.second,
                onAddressFound = { city, state, country ->
                    updateUserLocation(currentUser, pair, city, state, country)
                },
                onError = { e ->
                    Log.e("LocationUpdate", "Erro na geocodificação: ${e.message}", e)
                }
            )
        } else {
            Log.d("LocationUpdate", "Localização recente. Não é necessário atualizar.")
        }
    }

    /**
     * Busca usuários aleatórios da API remota.
     * @param page O número da página.
     * @return Uma lista de objetos [User].
     */
    suspend fun findRandomUsers(page: Int): List<User> {
        return try {
            val response = apiUserService.findRandom(_token.value, page, 15)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                showToast("Erro ao buscar usuários: ${response.code()}", context)
                emptyList()
            }
        } catch (error: Exception) {
            showToast("Erro ao buscar usuários", context)
            Log.e("UserSearch", "Erro: ${error.message}", error)
            emptyList()
        }
    }

    /**
     * Sincroniza os blocos de usuários com a API remota e o Realm.
     * @param page O número da página.
     * @param size O tamanho da página.
     */
    fun fetchBlockedUsers(page: Int, size: Int) {
        taskManager.addTask {
            handleBlockedUsersSync(page, size)
        }
    }

    /**
     * Retorna uma ImageMessage do Realm.
     * @param uuid UUID da imagem.
     * @return A ImageMessage correspondente, ou null.
     */
    fun getImageMessage(uuid: String): ImageMessage? {
        return realm.query<ImageMessageRealm>("uuid == $0", uuid).find().firstOrNull()
            ?.let { formatR.fromImageMessage(it) }
    }

    /**
     * Salva ou atualiza uma imagem de perfil no Realm.
     * @param imageProfile O objeto ImageProfile a ser salvo.
     * @param src Os bytes da imagem.
     */
    fun saveProfileImage(imageProfile: ImageProfile?, src: ByteArray) {
        try {
            if (imageProfile == null) return
            realm.writeBlocking {
                val existingImageRealm = query<ImageProfileRealm>("uuid == $0", imageProfile.uuid).first().find()
                if (existingImageRealm != null) {
                    existingImageRealm.src = src
                } else {
                    formatRealm.toImageProfileImage(imageProfile)?.let { newImageRealm ->
                        copyToRealm(newImageRealm, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RealmSave", "Erro ao salvar imagem de perfil: ${error.message}", error)
        }
    }

    /**
     * Salva ou atualiza uma imagem de mensagem no Realm.
     * @param imageMessage O objeto ImageMessage a ser salvo.
     * @param src Os bytes da imagem.
     */
    fun saveMessageImage(imageMessage: ImageMessage?, src: ByteArray) {
        try {
            if (imageMessage == null) return
            realm.writeBlocking {
                val existingImageRealm = query<ImageMessageRealm>("uuid == $0", imageMessage.uuid).first().find()
                if (existingImageRealm != null) {
                    existingImageRealm.src = src
                } else {
                    formatRealm.toImageMessage(imageMessage)?.let { newImageRealm ->
                        copyToRealm(newImageRealm, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RealmSave", "Erro ao salvar imagem de mensagem: ${error.message}", error)
        }
    }

    // --- Funções Auxiliares e Privadas ---

    /**
     * Inicializa todas as dependências do ViewModel (Realm, Retrofit, etc.).
     * @param context O contexto da aplicação.
     * @param providedRealm A instância do Realm, se houver.
     */
    private fun initializeDependencies(context: Context, providedRealm: Realm?) {
        val config = RealmConfiguration.Builder(
            schema = setOf(UserRealm::class, BlockRealm::class, ChatRealm::class, LocationRealm::class, ImageProfileRealm::class, ChatParticipantRealm::class, ImageMessageRealm::class, MessageRealm::class, PreferenceRealm::class, PrivacyUserRealm::class)
        ).schemaVersion(1).deleteRealmIfMigrationNeeded().build()

        this.realm = providedRealm ?: Realm.open(config)
        this.context = context
        this.baseUrl = context.getString(R.string.baseUrl)
        this.tokenManager = TokenManager(context)

        val retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
        this.apiServiceChat = retrofit.create(ChatEndPoint::class.java)
        this.apiUserService = retrofit.create(UsersEndpoints::class.java)
        this.userRequests = UserRequests(baseUrl, this.realm)
        this.chatRequests = ChatRequests(baseUrl, this.realm)
    }

    /**
     * Carrega o token e as informações do usuário do armazenamento local.
     */
    private fun loadUserAndTokenFromStorage() {
        val storedToken = tokenManager.getToken()
        if (storedToken != null) {
            _token.value = storedToken
            val userUuid = getSubFromJwt(storedToken)
            if (userUuid != null) {
                _myUser.value = getUserFromRealm(userUuid)
            } else {
                Log.e("UserViewModel", "Não foi possível extrair UUID do token.")
            }
        } else {
            Log.e("UserViewModel", "Nenhum token de usuário armazenado.")
        }
    }

    /**
     * Busca um usuário no Realm pelo UUID.
     * @param uuid O UUID do usuário.
     * @return O objeto [User] ou null.
     */
    private fun getUserFromRealm(uuid: String): User? {
        return realm.query<UserRealm>("uuid == $0", uuid).find().firstOrNull()?.let {
            formatR.fromUserRealm(it)
        }
    }

    /**
     * Lida com a lógica de upload de imagem, incluindo a conversão do URI para arquivo e o envio para a API.
     * @param imageUri O URI da imagem.
     * @param uuid UUID da imagem (opcional).
     * @param slot Posição da imagem.
     */
    private suspend fun handleImageUpload(imageUri: Uri, uuid: String?, slot: Int) {
        val tempFile = context.getTempFileFromUri(imageUri)
        try {
            val token = _token.value
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            val uploadedImageResponse = userRequests.uploadImage(body, token, slot + 1)

            uploadedImageResponse?.let {
                updateProfileStateAndRealm(it, tempFile.readBytes(), uuid)
            } ?: Log.e("ImageUpload", "Resposta da API de upload é nula.")

        } catch (e: Exception) {
            Log.e("ImageUpload", "Erro ao salvar imagem de perfil: ${e.message}", e)
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Atualiza o estado local e o Realm com a nova imagem de perfil.
     * @param uploadedImageResponse A resposta da API de upload.
     * @param imageBytes Os bytes da imagem.
     * @param uuid UUID da imagem.
     */
    private suspend fun updateProfileStateAndRealm(uploadedImageResponse: ImageProfile, imageBytes: ByteArray, uuid: String?) {
        val imageProfileWithBytes = uploadedImageResponse.copy(src = imageBytes, userUuid = _myUser.value?.uuid)
        val updatedProfileImages = if (uuid == null) {
            _myUser.value?.profileImages.orEmpty() + imageProfileWithBytes
        } else {
            _myUser.value?.profileImages.orEmpty().map { if (it.uuid == uuid) imageProfileWithBytes else it }
        }
        _myUser.value = _myUser.value?.copy(profileImages = updatedProfileImages.filterNotNull())

        realm.write {
            val userRealm = query<UserRealm>("uuid == $0", _myUser.value?.uuid).first().find()
            val imgRealm = formatRealm.toImageProfileImage(imageProfileWithBytes)
            if (userRealm != null && imgRealm != null) {
                userRealm.profileImages.add(copyToRealm(imgRealm, UpdatePolicy.ALL))
            }
        }
        Log.d("ImageUpload", "Imagem salva no Realm e estado local atualizado.")
    }

    /**
     * Lida com a lógica de atualização do usuário.
     * @param user O objeto [User] atualizado.
     * @param location A localização do usuário.
     * @param onFinally Lambda a ser executada.
     */
    private suspend fun handleUserUpdate(user: User, location: CreateLocation?, onFinally: () -> Unit) {
        try {
            val response = apiUserService.update(_token.value, UpdateUser(user.name, birthDate = user.birthDate, gender = user.gender, location = location))
            if (response.isSuccessful) {
                response.body()?.let { updatedUserDto ->
                    _myUser.value = updateUserInRealm(realm, user.uuid, updatedUserDto)
                    showToast("Sucesso!", context)
                }
            } else {
                response.errorBody()?.string()?.let { errorMessage ->
                    Log.e("UpdateUser", "Erro ao atualizar usuário: $errorMessage")
                    showToast("Erro ao atualizar usuário: $errorMessage", context)
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateUser", "Ocorreu um erro: ${e.message}", e)
            showToast("Ocorreu um erro: ${e.message}", context)
        } finally {
            onFinally()
        }
    }

    /**
     * Verifica se a última atualização da localização foi há mais de um dia.
     * @param lastUpdatedAt O timestamp da última atualização.
     * @return true se a localização estiver desatualizada, false caso contrário.
     */
    private fun isLocationOutdated(lastUpdatedAt: Long): Boolean {
        val oneDayInMillis = TimeUnit.DAYS.toMillis(1)
        return (System.currentTimeMillis() - lastUpdatedAt) >= oneDayInMillis
    }

    /**
     * Atualiza a localização do usuário no Realm e na API.
     * @param currentUser O objeto [User] atual.
     * @param pair O [Pair] de latitude e longitude.
     * @param city A cidade.
     * @param state O estado.
     * @param country O país.
     */
    private fun updateUserLocation(currentUser: User, pair: Pair<Double, Double>, city: String?, state: String?, country: String?) {
        realm.writeBlocking {
            var locationToPersist = this.query<LocationRealm>("userId == $0", currentUser.uuid).first().find()
            val userRealmToUpdate = this.query<UserRealm>("uuid == $0", currentUser.uuid).first().find()

            if (userRealmToUpdate == null) {
                Log.e("LocationUpdate", "UserRealm não encontrado.")
                return@writeBlocking
            }

            locationToPersist = locationToPersist ?: LocationRealm().apply {
                this.uuid = UUID.randomUUID().toString()
                this.userId = currentUser.uuid
                this.createdAt = System.currentTimeMillis()
            }

            locationToPersist.apply {
                this.latitude = pair.first.toString()
                this.longitude = pair.second.toString()
                this.city = city
                this.state = state
                this.updatedAt = System.currentTimeMillis()
            }

            userRealmToUpdate.location = copyToRealm(locationToPersist, UpdatePolicy.ALL)

            // Chama a função de atualização do usuário para a API
            val updatedUserDomain = currentUser.copy(
                location = Location(uuid = locationToPersist.uuid, latitude = locationToPersist.latitude, longitude = locationToPersist.longitude, city = locationToPersist.city, state = locationToPersist.state, userId = locationToPersist.userId, createdAt = locationToPersist.createdAt, updatedAt = locationToPersist.updatedAt)
            )
            updateUser(updatedUserDomain, CreateLocation(latitude = pair.first, longitude = pair.second, city = city, state = state, country = country)) {}
        }
    }

    /**
     * Lida com a sincronização de blocos de usuários.
     * @param page O número da página.
     * @param size O tamanho da página.
     */
    private suspend fun handleBlockedUsersSync(page: Int, size: Int) {
        try {
            val response = apiUserService.manyBlocks(_token.value, page, size)
            if (response.isSuccessful) {
                val apiBlocks = response.body() ?: emptyList()
                syncBlocksWithRealm(apiBlocks)
            } else {
                Log.e("BlocksGet", "Erro na busca de blocos da API: ${response.code()}")
                loadBlocksFromCache(page, size)
            }
        } catch (e: Exception) {
            Log.e("BlocksGet", "Erro crítico ao buscar ou salvar blocos: ${e.message}", e)
            loadBlocksFromCache(page, size)
        }
    }

    /**
     * Sincroniza a lista de blocos da API com o Realm.
     * @param apiBlocks A lista de blocos da API.
     */
    private fun syncBlocksWithRealm(apiBlocks: List<Block>) {
        val blocksRealmsToSave = apiBlocks.mapNotNull { formatRealm.toBlock(it) }
        realm.writeBlocking {
            blocksRealmsToSave.forEach { blockFromApi ->
                copyToRealm(blockFromApi, UpdatePolicy.ALL)
            }
        }
        _blocks.value = blocksRealmsToSave.map { formatR.fromBlockreal(it) }
        Log.d("BlocksGet", "Blocos sincronizados com sucesso no Realm.")
    }

    /**
     * Carrega blocos do cache local (Realm) como fallback.
     * @param page O número da página.
     * @param size O tamanho da página.
     */
    private fun loadBlocksFromCache(page: Int, size: Int) {
        val localBlocks = realm.query<BlockRealm>().limit(page * size).find()
        _blocks.value = localBlocks.map { formatR.fromBlockreal(it) }
    }

    /**
     * Copia o conteúdo de um Uri para um arquivo temporário.
     * @param uri O Uri do arquivo.
     * @return Um objeto File temporário.
     */
    private suspend fun Context.getTempFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile("temp_image", ".jpg", cacheDir)
        }
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
}