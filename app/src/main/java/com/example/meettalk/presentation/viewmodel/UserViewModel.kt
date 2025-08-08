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
import com.example.meettalk.data.local.model.RealmClass.BlockRealm
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageMessageRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.LocationRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.RealmClass.PrivacyUserRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.CreateLocation
import com.example.meettalk.data.local.model.body.UpdateUser
import com.example.meettalk.data.local.model.entities.Block
import com.example.meettalk.data.local.model.entities.ImageMessage
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Location
import com.example.meettalk.data.local.model.entities.User
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

class UserViewModel : ViewModel() {
    private val formatR = FormatClass()
    private var baseUrl = ""
    private val _taskManager = TaskManager()

    private lateinit var realm: Realm
    private lateinit var context: Context
    private lateinit var tokenManager: TokenManager
    private lateinit var apiServiceChat: ChatEndPoint
    private lateinit var apiUserService: UsersEndpoints
    private lateinit var chatRequests: ChatRequests

    private val _myUser = MutableStateFlow<User?>(null)
    val myUser: StateFlow<User?> = _myUser.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _uiState = MutableStateFlow<UiState>(UiState.Success())

    @RequiresApi(Build.VERSION_CODES.O)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val format = FormatRealm()
    private lateinit var userRequests: UserRequests

    private val _users = MutableStateFlow(emptyList<User>())
    val users = _users.asStateFlow()

    private val _blocks = MutableStateFlow(emptyList<Block>())
    val blocks = _blocks.asStateFlow()

    fun getImageMessage(uuid: String): ImageMessage? {
        return try {
            realm.query<ImageMessageRealm>("uuid == $0", uuid).find().firstOrNull()
                ?.let { formatR.fromImageMessage(it) }
        } catch (error: Exception) {
            println(error.message)
            null
        }
    }

    // Dentro do UserViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun handleTokenValidation(context: Context): String {
        val tokenManager = TokenManager(context)
        val localToken = tokenManager.getToken()

        if (!localToken.isNullOrBlank() && !isJwtExpired(localToken.replace("Bearer ", ""))) {
            // Token local é válido, vai para a lista de chats
            return AppRoutes.CHAT_LIST
        }

        try {
            val newToken = chatRequests.refreshAndGetToken()
            if (!newToken.isNullOrBlank()) {
                tokenManager.saveToken(newToken)
                return AppRoutes.CHAT_LIST
            }
        } catch (e: Exception) {
            Log.e("AuthError", "Falha ao buscar novo token", e)
        }

        return AppRoutes.LOGIN
    }

    /**
     * Constrói e inicializa as dependências do ViewModel.
     * Esta função deve ser chamada uma única vez quando o ViewModel é criado.
     *
     * @param context Contexto da aplicação, necessário para acessar recursos e TokenManager.
     * @param realm Instância opcional do Realm. Se nula, uma nova instância será aberta.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(context: Context, realm: Realm? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                if (realm == null) {
                    val config = RealmConfiguration.Builder(
                        schema = setOf(
                            UserRealm::class,
                            BlockRealm::class,
                            ChatRealm::class,
                            LocationRealm::class,
                            ImageProfileRealm::class,
                            ChatParticipantRealm::class,
                            ImageMessageRealm::class,
                            MessageRealm::class,
                            PreferenceRealm::class,
                            PrivacyUserRealm::class
                        )
                    ).schemaVersion(1).deleteRealmIfMigrationNeeded().build()

                    this@UserViewModel.realm = Realm.open(config)
                } else {
                    this@UserViewModel.realm = realm
                }

                this@UserViewModel.context = context
                this@UserViewModel.baseUrl = context.getString(R.string.baseUrl)
                this@UserViewModel.tokenManager = TokenManager(context)
                this@UserViewModel.userRequests = UserRequests(baseUrl, this@UserViewModel.realm)
                val retrofit = Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create()).build()

                this@UserViewModel.apiServiceChat = retrofit.create(ChatEndPoint::class.java)
                this@UserViewModel.apiUserService = retrofit.create(UsersEndpoints::class.java)
                this@UserViewModel.chatRequests = ChatRequests(baseUrl, this@UserViewModel.realm)

                val storedToken = tokenManager.getToken()
                if (storedToken != null) {
                    _token.value = storedToken // adicinando valo do token
                    val userUuid = getSubFromJwt(storedToken)
                    if (userUuid != null) {
                        val user = getUser(userUuid)
                        if (user != null) {
                            _myUser.value = user
                            _uiState.value =
                                UiState.Success(user)
                        } else {
                            _uiState.value =
                                UiState.Error("Usuário não encontrado localmente para o token fornecido.")
                        }
                    } else {
                        _uiState.value = UiState.Error("Não foi possível extrair UUID do token.")
                    }
                } else {
                    _uiState.value = UiState.Error("Nenhum token de usuário armazenado.")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value =
                    UiState.Error("Falha na inicialização: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }
    }

    /**
     * Encontra um usuário no Realm pelo UUID.
     * @param uuid O UUID do usuário a ser encontrado.
     * @return O objeto User correspondente ou null se não for encontrado ou ocorrer um erro.
     */
    fun findUser(uuid: String): User? {
        return try {
            realm.query<UserRealm>("uuid == $0", uuid).first().find()?.let {
                formatR.fromUserRealm(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copia o conteúdo de um Uri para um arquivo temporário no diretório de cache do aplicativo.
     *
     * @param uri O Uri do arquivo original.
     * @return Um objeto File temporário contendo os dados, ou null em caso de erro.
     * É responsabilidade do chamador deletar este arquivo após o uso.
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

    /**
     * Busca um usuário aleatório da API remota.
     *
     * Esta função realiza uma chamada assíncrona para o endpoint de busca de usuário aleatório,
     * utilizando um token de autenticação.
     *
     * @return Retorna um objeto [User] se a requisição for bem-sucedida e um usuário for encontrado.
     * Retorna `null` em caso de erro na requisição (ex: falha de rede, erro do servidor)
     * ou se nenhum usuário for retornado. Erros são impressos no logcat.
     *
     * @throws Exception Se ocorrer uma exceção inesperada durante a chamada da API (já tratada internamente para retornar null).
     */
    suspend fun randomUser(): User? {
        return try {
            userRequests.random(_token.value)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setUsers(list: List<User>) {
        _users.value = list
    }

    private fun saveProfileImageApi(imageUri: Uri, uuid: String? = null, slot: Int = 0) {
        viewModelScope.launch {
            val contentResolver = context.contentResolver
            val type = contentResolver.getType(imageUri) ?: "image/jpeg"

            val tempFile = context.getTempFileFromUri(imageUri)

            try {
                val token = _token.value ?: run {
                    Log.w(
                        "ImageUpload",
                        "Token de autenticação não encontrado. Abortando upload de imagem."
                    )
                    return@launch
                }

                val requestFile = tempFile.asRequestBody(type.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                val uploadedImageResponse = userRequests.uploadImage(
                    file = body,
                    token = token,
                    slot = slot + 1
                )

                if (uploadedImageResponse == null) {
                    Log.e("ImageUpload", "Resposta da API de upload de imagem é nula.")
                    return@launch
                }

                val imageProfileWithBytes = uploadedImageResponse
                    .copy(
                        src = tempFile.readBytes(),
                        userUuid = _myUser.value?.uuid,
                        user = _myUser.value
                    )

                val updatedProfileImages = if (uuid == null) {
                    _myUser.value?.profileImages.orEmpty() + imageProfileWithBytes
                } else {
                    _myUser.value?.profileImages.orEmpty().map {
                        if (it.uuid == uuid) imageProfileWithBytes else it
                    }
                }

                _myUser.value =
                    _myUser.value?.copy(profileImages = updatedProfileImages.filterNotNull())

                realm.write {
                    val existingRealmImg =
                        query<ImageProfileRealm>("uuid == $0", imageProfileWithBytes.uuid).find()
                            .firstOrNull()
                    val userRealm =
                        query<UserRealm>("uuid == $0", imageProfileWithBytes.userUuid).find()
                            .firstOrNull()

                    if (existingRealmImg == null) {
                        imageProfileWithBytes.toRealm()?.let {
                            userRealm?.profileImages?.add(it)
                        }
                    }
                }
                Log.d("ImageUpload", "Imagem salva no Realm e estado local atualizado.")

            } catch (e: Exception) {
                Log.e("ImageUpload", "Erro ao salvar imagem de perfil: ${e.message}", e)
            } finally {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.d(
                            "ImageUpload",
                            "Arquivo temporário deletado: ${tempFile.absolutePath}"
                        )
                    } else {
                        Log.e(
                            "ImageUpload",
                            "Falha ao deletar arquivo temporário: ${tempFile.absolutePath}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Salva ou atualiza uma imagem de perfil no Realm.
     * @param imageProfile O objeto ImageProfile a ser salvo/atualizado.
     * @param src Os bytes da imagem.
     */
    fun saveProfileImage(imageProfile: ImageProfile?, src: ByteArray) {
        try {
            if (imageProfile == null) return
            val profileRealm = format.toImageProfileImage(imageProfile)

            realm?.writeBlocking {
                val imageGet =
                    this.query<ImageProfileRealm>("uuid == $0", profileRealm?.uuid).first().find()

                if (imageGet != null) {
                    imageGet.src = src
                } else {
                    profileRealm?.let {
                        copyToRealm(
                            it, updatePolicy = UpdatePolicy.ALL
                        )
                    }
                }
            }
        } catch (error: Exception) {
            error.printStackTrace()
            Log.e("Error", error.message.toString())
        }
    }

    /**
     * Salva ou atualiza uma imagem de perfil no Realm.
     * @param imageMessage O objeto ImageProfile a ser salvo/atualizado.
     * @param src Os bytes da imagem.
     */
    fun saveMessageImage(imageMessage: ImageMessage?, src: ByteArray) {
        try {
            if (imageMessage == null) return
            val profileRealm = format.toImageMessage(imageMessage)

            realm.writeBlocking {
                val imageGet =
                    this.query<ImageProfileRealm>("uuid == $0", profileRealm?.uuid).first().find()

                if (imageGet != null) {
                    imageGet.src = src
                } else {
                    copyToRealm(
                        profileRealm, updatePolicy = UpdatePolicy.ALL
                    )
                }
            }
        } catch (error: Exception) {
            error.printStackTrace()
            Log.e("Error", error.message.toString())
        }
    }

    suspend fun getUser(uuid: String, requestOn: Boolean = false): User? {
        return try {
            if (!requestOn) {
                var user = users.value.find { it.uuid == uuid }
                    ?: realm.query<UserRealm>("uuid == $0", uuid).find().firstOrNull()?.let {
                        formatR.fromUserRealm(it)
                    }
                if (user == null) {
                    user = userRequests.getUser(uuid, _token.value)
                }
                user
            } else {
                val user = userRequests.getUser(uuid, _token.value)
                user
            }
        } catch (error: Exception) {
            error.printStackTrace()
            null
        }
    }

    suspend fun getUserRequest(uuid: String): User? {
        return userRequests.getUser(uuid, token.value)
    }

    /**
     * Verifica a validade de um token no backend.
     * @param token O token a ser verificado.
     * @return true se o token for válido, false caso contrário.
     */
    suspend fun verifyToken(token: String): Boolean {
        return try {
            val response = apiServiceChat.verifyToken(token) // Chama a API para verificar o token
            response.isSuccessful // Retorna true se a resposta for bem-sucedida
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lida com o upload de uma imagem de perfil a partir de um URI.
     * Esta função é um placeholder e deve ser expandida para incluir a lógica de upload real
     * (ex: leitura do URI, conversão para bytes, envio para um serviço de armazenamento).
     *
     * @param uri O URI da imagem selecionada.
     */
    fun uploadProfileImage(uri: Uri, uuid: String? = null, currentImageIndex: Int = 0) {
        viewModelScope.launch {
            saveProfileImageApi(uri, uuid, currentImageIndex)
        }
    }

    /**
     * Atualiza as informações de um usuário no servidor e no banco de dados local (Realm).
     *
     * @param user O objeto [User] contendo as informações atualizadas do usuário.
     */
    @JvmOverloads
    fun updateUser(user: User, location: CreateLocation? = null, onFinally: () -> Unit) {
        _taskManager.addTask {
            try {
                val token = _token.value

                val response = apiUserService.update(
                    token,
                    UpdateUser(
                        user.name,
                        birthDate = user.birthDate,
                        gender = user.gender,
                        location = location
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { updatedUserDto ->
                        _myUser.value = updateUserInRealm(realm, user.uuid, updatedUserDto)
                    } ?: run {
                        showToast("Sucesso, mas resposta da API vazia.", context)
                    }
                    showToast("Sucesso!", context)
                } else {
                    response.errorBody()?.string()?.let { errorMessage ->
                        println(errorMessage)
                        showToast("Erro ao atualizar usuário: $errorMessage", context)
                    } ?: run {
                        Log.e("Error de Localização", "${response.code()}")
                        showToast("Erro desconhecido ao atualizar usuário. Código: ${response.code()}", context)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.let {
                    Log.e("Error de Localização", "${it}")
                    showToast("Ocorreu um erro: $it", context)
                } ?: showToast("Ocorreu um erro inesperado ao atualizar o usuário.", context)
            } finally {
                onFinally()
            }
        }
    }

    /**
     * Insere a localização geográfica atual do usuário, convertendo as coordenadas
     * para cidade e estado antes de atualizar os dados do usuário.
     *
     * Esta função realiza os seguintes passos:
     * 1. Cria ou recupera um objeto [LocationRealm] para o usuário.
     * 2. Verifica se a última atualização da localização foi há mais de um dia.
     * 3. Se a condição for atendida, utiliza a geocodificação reversa ([getAddressFromLocation]) para obter
     * a cidade, estado e país correspondentes às coordenadas.
     * 4. Atualiza o objeto [LocationRealm] com os dados geocodificados e o timestamp de atualização.
     * 5. Persiste as mudanças no Realm, incluindo a referência da LocationRealm no UserRealm.
     *
     * @param context O contexto do aplicativo, necessário para Geocoder.
     * @param realm A instância do Realm.
     * @param pair Um [Pair] onde [pair.first] representa a latitude (Double)
     * e [pair.second] representa a longitude (Double) da localização.
     */
    fun insertLocation(pair: Pair<Double, Double>) {
        val currentUser = _myUser.value

        if (currentUser == null || currentUser.uuid.isEmpty()) {
            Log.e(
                "LocationUpdate",
                "Usuário atual inválido ou sem UUID. Não é possível atualizar a localização."
            )
            return
        }

        val userId = currentUser.uuid
        val ONE_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)
        val currentTime = System.currentTimeMillis()

        val existingLocationRealm =
            realm.query<LocationRealm>("userId == $0", userId).first().find()

        val shouldUpdate = existingLocationRealm == null ||
                (currentTime - existingLocationRealm.updatedAt >= ONE_DAY_IN_MILLIS)

        if (shouldUpdate) {
            Log.d("LocationUpdate", "Iniciando atualização de localização para o usuário: $userId")

            getAddressFromLocation(
                context,
                latitude = pair.first,
                longitude = pair.second,
                onAddressFound = { city, state, country ->
                    realm.writeBlocking {
                        var locationToPersist = this.query<LocationRealm>("userId == $0", userId).first().find()

                        val userRealmToUpdate = this.query<UserRealm>("uuid == $0", userId).first().find()

                        if (userRealmToUpdate == null) {
                            Log.e(
                                "LocationUpdate",
                                "UserRealm não encontrado na transação para UUID: $userId"
                            )
                            return@writeBlocking
                        }

                        if (locationToPersist == null) {
                            locationToPersist = LocationRealm().apply {
                                this.uuid = UUID.randomUUID().toString() // Garante UUID único
                                this.userId = userId
                                this.createdAt = currentTime
                            }
                        }

                        locationToPersist.apply {
                            this.latitude = pair.first.toString()
                            this.longitude = pair.second.toString()
                            this.city = city
                            this.state = state
                            this.updatedAt = currentTime
                        }

                        val managedLocationRealm = copyToRealm(locationToPersist, UpdatePolicy.ALL)

                        userRealmToUpdate.location = managedLocationRealm

                        currentUser.let { userDomain ->
                            val updatedLocationDomain = Location(
                                uuid = managedLocationRealm.uuid,
                                latitude = pair.first.toString(),
                                longitude = pair.second.toString(),
                                city = city,
                                state = state,
                                userId = userId,
                                createdAt = managedLocationRealm.createdAt,
                                updatedAt = managedLocationRealm.updatedAt
                            )

                            val updatedUserDomain = userDomain.copy(
                                location = updatedLocationDomain,
                            )
                            updateUser(
                                user = updatedUserDomain,
                                location = CreateLocation(
                                    latitude = pair.first,
                                    longitude = pair.second,
                                    city = city,
                                    state = state,
                                    country = country
                                )
                            ) {
                            }
                        }
                        Log.d(
                            "LocationUpdate",
                            "Localização Realm atualizada/criada com sucesso para o usuário: $userId"
                        )
                    }
                },
                onError = { e ->
                    Log.e(
                        "LocationUpdate",
                        "Erro na geocodificação para o usuário $userId: ${e.message}",
                        e
                    )
                }
            )
        } else {
            Log.d(
                "LocationUpdate",
                "Localização não atualizada para o usuário $userId. Menos de um dia desde a última atualização."
            )
        }
    }

    suspend fun findRandomUsers(page: Int): List<User> {
        val token = _token.value
        return try {
            val response = apiUserService.findRandom(
                token, page, 15
            )

            return if (response.isSuccessful) response.body() ?: emptyList()
            else {
                println(response.errorBody()?.string())
                showToast("Error ao procurar", context)
                emptyList()
            }

        } catch (error: Exception) {
            showToast("Error ao procurar", context)
            error.printStackTrace()
            emptyList()
        }
    }

    /**
     * Sincroniza blocos de dados entre a API remota e o banco de dados local Realm.
     *
     * Esta função realiza a seguinte sequência de operações:
     * 1. Inicia uma tarefa assíncrona no gerenciador de tarefas [_taskManager].
     * 2. Tenta buscar os blocos da API usando os parâmetros de paginação [page] e [size].
     * 3. Caso a chamada à API seja bem-sucedida:
     *    - Converte os blocos recebidos da API para o modelo do Realm.
     *    - Salva os blocos no banco local com política de atualização total.
     *    - Atualiza o estado [_blocks] com os dados sincronizados.
     * 4. Caso a chamada à API falhe:
     *    - Registra o erro no log.
     *    - Carrega blocos do cache local limitado a [page * size] e atualiza [_blocks].
     * 5. Em caso de exceção durante a operação:
     *    - Registra o erro no log e exibe rastreamento.
     *    - Carrega blocos do cache local como fallback e atualiza [_blocks].
     *
     * @param page Número da página a ser buscada (começando em 1).
     * @param size Quantidade de blocos por página.
     *
     * Requisitos:
     * - [_token] deve conter um token de autenticação válido.
     * - [apiUserService] deve estar configurado para comunicação com o backend.
     * - [realm], [format], [formatR] e [_blocks] devem estar corretamente inicializados.
     *
     * Logs:
     * - Sucesso de sincronização.
     * - Falhas na chamada da API.
     * - Erros de exceção.
     */
    fun fetchBlockedUsers(page: Int, size: Int) {
        val token = _token.value
        _taskManager.addTask {
            try {
                val localBlocks = realm.query<BlockRealm>().limit(page * size).find()

                val response = apiUserService.manyBlocks(token, page, size)

                if (response.isSuccessful) {
                    val apiBlocks = response.body() ?: emptyList()
                    val blocksRealmsToSave = apiBlocks.map { format.toBlock(it) }

                    if (blocksRealmsToSave.isNotEmpty()) {
                        realm.write {
                            blocksRealmsToSave.forEach { blockFromApi ->
                                copyToRealm(blockFromApi, UpdatePolicy.ALL)
                            }
                        }
                        Log.d("BlocksGet", "Blocos sincronizados com sucesso no Realm.")

                        _blocks.value = blocksRealmsToSave.map { formatR.fromBlockreal(it) }

                    } else {
                        Log.d(
                            "BlocksGet",
                            "Nenhum bloco retornado pela API. Nada para sincronizar, mas a chamada foi bem-sucedida."
                        )
                    }
                } else {
                    Log.e(
                        "BlocksGet",
                        "Erro na busca de blocos da API: ${response.code()} - ${
                            response.errorBody()?.string()
                        }. Carregando do cache local."
                    )
                    _blocks.value = localBlocks.map { formatR.fromBlockreal(it) }
                }

            } catch (e: Exception) {
                Log.e("BlocksGet", "Erro crítico ao buscar ou salvar blocos: ${e.message}", e)
                e.printStackTrace()
                val localBlocksOnError = realm.query<BlockRealm>().limit(page * size).find()
                _blocks.value = localBlocksOnError.map { formatR.fromBlockreal(it) }
            }
        }
    }
}
