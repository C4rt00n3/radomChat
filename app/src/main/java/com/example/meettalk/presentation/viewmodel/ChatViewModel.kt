package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.RealmClass.*
import com.example.meettalk.data.local.model.entities.*
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.data.repository.SocketManager
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TaskManager
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.getSubFromJwt
import com.example.meettalk.utils.users.updateUserInRealm
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Base64

/**
 * ViewModel responsável por gerenciar a lógica de negócios da tela de chats.
 * Lida com a conexão com o servidor via Retrofit e WebSockets, gerenciamento de dados
 * no Realm e manipulação de eventos de chat.
 */
open class ChatViewModel : ViewModel() {

    // --- Variáveis de Estado Públicas ---

    private val _chatsResult = MutableStateFlow<List<Chat>>(listOf())

    /** Representa a lista de chats do usuário, exposta como um StateFlow para a UI. */
    open val chatsResult: StateFlow<List<Chat>> = _chatsResult

    private val _selectedChats = MutableStateFlow<List<Chat>>(listOf())
    val selectedChats: StateFlow<List<Chat>> = _selectedChats

    private val _myUser = MutableStateFlow<User?>(null)

    /** Representa o usuário logado, exposto como um StateFlow. */
    open val myUser: StateFlow<User?> = _myUser

    private val _isSocketConnected = MutableStateFlow(false)

    /** Indica se a conexão com o WebSocket está ativa, exposto como um StateFlow. */
    open val isSocketConnected: StateFlow<Boolean> = _isSocketConnected

    // --- Ferramentas e Dependências Privadas ---

    private val gson = Gson()
    private val formatRealm = FormatRealm()
    private val taskManager = TaskManager()

    private lateinit var realm: Realm
    private lateinit var retrofit: Retrofit
    private lateinit var apiServiceChat: ChatEndPoint
    private lateinit var context: Context
    private lateinit var chatRequests: ChatRequests
    private lateinit var token: String
    private lateinit var baseUrl: String
    private var socketManager: SocketManager? = null

    // --- Modelos de Dados Privados ---

    private data class MessageReady(val chatId: String, val userId: String)
    private data class MessageUUID(val uuid: String)

    // --- Métodos Públicos ---

    /**
     * Inicializa o ViewModel com as dependências necessárias.
     * @param context O contexto da aplicação para acesso a recursos.
     * @param realm A instância do Realm (opcional, para testes).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun build(context: Context, realm: Realm? = null) {
        try {
            this.context = context
            this.baseUrl = context.getString(R.string.baseUrl)

            initializeRealm(realm)
            initializeRetrofitAndRequests()
            loadUserFromToken()
            connectSocket()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro na inicialização: ${e.message}", e)
        }
    }

    fun removeSelectedChats(chat: Chat) {
        _selectedChats.value -= chat
    }

    fun addSelectedChats(chat: Chat) {
        _selectedChats.value += chat
    }

    fun allSelectedChats(chats: List<Chat>) {
        _selectedChats.value = chats
    }


    /**
     * Conecta-se ao servidor WebSocket usando o token de autenticação.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun connectSocket() {
        try {
            val formattedUrl = "$baseUrl/?token=${token.replace("Bearer ", "")}"
            socketManager = SocketManager(formattedUrl, context)
            socketManager?.let { manager ->
                com.example.meettalk.data.repository.connectSocket(manager, this) {
                    _isSocketConnected.value = it
                }
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro ao conectar ao socket: ${e.message}", e)
        }
    }

    /**
     * Observa mudanças nos chats do Realm e atualiza o StateFlow.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    open fun observeChats() {
        viewModelScope.launch {
            realm.query<ChatRealm>().asFlow().collect {
                // Atualiza o StateFlow com os chats mais recentes do Realm
                _chatsResult.value = findChatsRealm()
            }
        }
    }

    /**
     * Busca os chats no banco de dados local ou no servidor, se o local estiver vazio.
     */
    suspend fun findChats() {
        val chatsFromRealm = findChatsRealm()
        _chatsResult.value = chatsFromRealm.ifEmpty {
            fetchChatsFromServerAndInsert()
        }
    }

    // --- Funções de Manipulação de Eventos do Socket ---

    /**
     * Processa o evento 'newChats' do WebSocket, atualizando os chats e suas mensagens no Realm.
     * @param args Array de argumentos recebidos do socket. O primeiro argumento deve ser o payload JSON.
     */
    suspend fun newChats(args: Array<Any>) = handleSocketEvent(args, "newChats") { jsonString ->
        data class ChatsPayload(val chats: List<Chat>)

        val chatsPayload = gson.fromJson(jsonString, ChatsPayload::class.java)

        chatsPayload?.chats?.let { chats ->
            realm.write { // Transação de escrita do Realm
                chats.forEach { chatJson ->
                    // Chama a função auxiliar, passando o contexto de escrita 'this' (MutableRealm)
                    updateOrInsertChat(this@write, chatJson)
                }
            }
        } ?: Log.w("ChatViewModel", "Nenhum chat válido encontrado no payload JSON.")
    }

    /**
     * Processa o evento 'contactImageProfileUpdated', atualizando a imagem de perfil de um contato.
     * @param args Array de argumentos recebidos do socket.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun contactImageProfileUpdated(args: Array<Any>) =
        handleSocketEvent(args, "contactImageProfileUpdated") { jsonString ->
            gson.fromJson(jsonString, ContactImageProfileUpdated::class.java)?.let { res ->
                val decodedBytes = decodeBase64Image(res.data.src)
                val imageProfile = ImageProfile(
                    uuid = res.data.uuid,
                    userUuid = res.data.userUuid,
                    src = decodedBytes,
                    createAt = res.data.createAt,
                    updateAt = res.data.updateAt,
                    slot = res.data.slot,
                )
                decodedBytes?.let {
                    updateImageProfileInRealm(
                        imageProfile, it
                    )
                    updateChatsStateWithImage(res.data.userUuid, imageProfile, it)
                }
            }
        }

    /**
     * Processa o evento 'contactProfileUpdated', atualizando o perfil de um contato no Realm.
     * @param args Array de argumentos recebidos do socket.
     */
    fun contactProfileUpdated(args: Array<Any>) =
        handleSocketEvent(args, "contactProfileUpdated") { jsonString ->
            val data = gson.fromJson(jsonString, ContactProfileUpdated::class.java)
            updateUserInRealm(realm, data.userId, data.data)
            // A UI observando `_chatsResult` ou `_myUser` será atualizada automaticamente
        }

    /**
     * Processa o evento 'removeMessages', excluindo mensagens específicas do Realm.
     * @param args Array de argumentos recebidos do socket.
     */
    fun removeMessages(args: Array<Any>) = handleSocketEvent(args, "removeMessages") { jsonString ->
        val listType = object : TypeToken<List<MessageUUID>>() {}.type
        val messages: List<MessageUUID> = gson.fromJson(jsonString, listType)

        if (messages.isNotEmpty()) {
            realm.writeBlocking {
                val messagesToDelete =
                    this.query<MessageRealm>("uuid IN $0", messages.map { it.uuid }).find()
                delete(messagesToDelete)
            }
        }
    }

    /**
     * Processa o evento 'updateMessage', atualizando o conteúdo de uma mensagem no Realm.
     * @param args Array de argumentos recebidos do socket.
     */
    suspend fun updateMessage(args: Array<Any>) =
        handleSocketEvent(args, "updateMessage") { jsonString ->
            val incomingMessage = gson.fromJson(jsonString, Message::class.java)
            realm.write {
                val messageRealm =
                    query<MessageRealm>("uuid == $0", incomingMessage.uuid).first().find()
                messageRealm?.text = incomingMessage.text
                Log.d("ChatViewModel", "Mensagem Realm ${incomingMessage.uuid} atualizada.")
            }
        }

    /**
     * Processa o evento 'onMessageReceived', adicionando uma nova mensagem ao chat correspondente no Realm.
     * @param args Array de argumentos recebidos do socket.
     */
    suspend fun onMessageReceived(args: Array<Any>) =
        handleSocketEvent(args, "onMessageReceived") { jsonString ->
            val messageModel = gson.fromJson(jsonString, Message::class.java).copy(isSend = true)
            val chatRealm = findChatOrCreateIfNotFound(messageModel.chatId)

            chatRealm?.let { chat ->
                realm.write {
                    val liveChat = findLatest(chat)
                    liveChat?.let {
                        val messageRealm = formatRealm.toMessageRealm(messageModel.copy())
                        messageRealm?.let { msgRealm ->
                            liveChat.messages.add(msgRealm)
                            liveChat.lastMessageDate = messageModel.createdAt
                            Log.d(
                                "ChatViewModel",
                                "Mensagem adicionada ao chat ${messageModel.chatId}."
                            )
                        }
                    }
                }
            } ?: Log.e("ChatViewModel", "Chat não encontrado para a mensagem.")
        }

    /**
     * Processa o evento 'listMessageRemoved', removendo uma lista de mensagens do Realm.
     * @param args Array de argumentos recebidos do socket.
     */
    suspend fun listMessageRemoved(args: Array<Any>) =
        handleSocketEvent(args, "listMessageRemoved") { jsonString ->
            val type = object : TypeToken<List<String>>() {}.type
            val removedMessageUuids: List<String> = gson.fromJson(jsonString, type)

            realm.write {
                val messagesToDelete =
                    this.query<MessageRealm>("uuid IN $0", removedMessageUuids).find()
                delete(messagesToDelete)
                Log.d("ChatViewModel", "Mensagens removidas do Realm: $removedMessageUuids")
            }
        }

    /**
     * Processa o evento 'messageReady', marcando mensagens como lidas para um usuário específico em um chat.
     * @param args Array de argumentos recebidos do socket.
     */
    suspend fun messageReady(args: Array<Any>) =
        handleSocketEvent(args, "messageReady") { jsonString ->
            val messageReady = gson.fromJson(jsonString, MessageReady::class.java)

            realm.write {
                val chatRealm = query<ChatRealm>("uuid == $0", messageReady.chatId).first().find()
                chatRealm?.messages?.forEach { message ->
                    if (message.senderId == messageReady.userId) {
                        message.isRead = true
                    }
                }
                Log.d(
                    "ChatViewModel",
                    "Mensagens marcadas como lidas para chat ${messageReady.chatId}."
                )
            }
        }

    // --- Métodos Auxiliares e Privados ---

    /**
     * Encontra um chat no Realm pelo UUID ou o cria se não existir, buscando na API.
     * @param chatId O UUID do chat.
     * @return O ChatRealm encontrado ou criado.
     */
    private suspend fun findChatOrCreateIfNotFound(chatId: String?): ChatRealm? {
        if (chatId == null) return null

        var chatRealm = realm.query<ChatRealm>("uuid == $0", chatId).first().find()
        if (chatRealm == null) {
            val apiChat = chatRequests.findOneChat(chatId, token)
            apiChat?.let {
                val newChatRealm = formatRealm.toChat(it)
                if (newChatRealm != null) {
                    realm.write { chatRealm = copyToRealm(newChatRealm, UpdatePolicy.ALL) }
                }
            }
        }
        return chatRealm
    }

    /**
     * Extrai o conteúdo JSON de um evento de socket e o processa.
     * @param args Array de argumentos do socket.
     * @param eventName O nome do evento para logging.
     * @param onValidJson Uma função de lambda a ser executada com o JSON string.
     */
    private inline fun handleSocketEvent(
        args: Array<Any>,
        eventName: String,
        crossinline onValidJson: suspend (String) -> Unit
    ) {
        if (args.isEmpty()) {
            Log.w("ChatViewModel", "Argumentos vazios para o evento '$eventName'.")
            return
        }

        val jsonString = when (val data = args[0]) {
            is String -> data
            is JSONObject -> data.toString()
            else -> {
                Log.w(
                    "ChatViewModel",
                    "Formato de dado inesperado para '$eventName': ${data::class.java.simpleName}."
                )
                return
            }
        }

        try {
            viewModelScope.launch {
                onValidJson(jsonString)
            }
        } catch (e: JsonSyntaxException) {
            Log.e("ChatViewModel", "Erro de sintaxe JSON no evento '$eventName': ${e.message}", e)
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro inesperado no evento '$eventName': ${e.message}", e)
        }
    }

    /**
     * Inicializa a instância do Realm.
     * @param realm A instância do Realm fornecida (se houver).
     */
    private fun initializeRealm(realm: Realm?) {
        if (realm != null) {
            this.realm = realm
        } else {
            val config = RealmConfiguration.Builder(
                schema = setOf(
                    UserRealm::class, BlockRealm::class, ChatRealm::class, LocationRealm::class,
                    ImageProfileRealm::class, ChatParticipantRealm::class, ImageMessageRealm::class,
                    MessageRealm::class, PreferenceRealm::class, PrivacyUserRealm::class
                )
            ).schemaVersion(1).deleteRealmIfMigrationNeeded().build()
            this.realm = Realm.open(config)
        }
    }

    /**
     * Inicializa o Retrofit e a API de chat.
     */
    private fun initializeRetrofitAndRequests() {
        this.retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        this.apiServiceChat = this.retrofit.create(ChatEndPoint::class.java)
        this.chatRequests = ChatRequests(baseUrl, realm)
    }

    /**
     * Carrega o usuário atual a partir do token JWT e o armazena no StateFlow.
     */
    private fun loadUserFromToken() {
        val storedToken = TokenManager(context).getToken()
        if (storedToken != null) {
            token = storedToken
            getSubFromJwt(token)?.let { userUuid ->
                val user = realm.query<UserRealm>("uuid == $0", userUuid).find().firstOrNull()
                _myUser.value = user?.toClass()
            }
        }
    }

    /**
     * Atualiza ou insere um chat no Realm.
     * @param chatJson O objeto Chat a ser atualizado/inserido.
     */
    private fun updateOrInsertChat(realm: MutableRealm, chatJson: Chat) {
        val existingChatRealm = realm.query<ChatRealm>("uuid == $0", chatJson.uuid).first().find()
        if (existingChatRealm == null) {
            formatRealm.toChat(chatJson)?.let { newChatRealm ->
                realm.copyToRealm(newChatRealm, UpdatePolicy.ALL)
            }
        } else {
            existingChatRealm.lastMessageDate = chatJson.lastMessageDate
            // Chama a função auxiliar de sincronização de mensagens
            syncMessages(realm, existingChatRealm, chatJson.messages)
        }
    }

    /**
     * Sincroniza as mensagens de um chat no Realm com as mensagens do payload.
     * @param chatRealm O objeto ChatRealm a ser sincronizado.
     * @param newMessages A lista de mensagens do payload.
     */
    private fun syncMessages(
        realm: MutableRealm,
        chatRealm: ChatRealm,
        newMessages: List<Message>
    ) {
        newMessages.forEach { messageJson ->
            val existingMessageRealm =
                chatRealm.messages.query<MessageRealm>("uuid == $0", messageJson.uuid).first()
                    .find()
            if (existingMessageRealm == null) {
                messageJson.toRealm()?.let { newMessageRealm ->
                    chatRealm.messages.add(newMessageRealm)
                }
            }
        }
    }

    /**
     * Decodifica uma string Base64 em um array de bytes.
     * @param base64String A string Base64.
     * @return O array de bytes decodificado ou null em caso de erro.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun decodeBase64Image(base64String: String): ByteArray? {
        return try {
            Base64.getDecoder().decode(base64String)
        } catch (e: IllegalArgumentException) {
            Log.e("ChatViewModel", "Erro ao decodificar Base64: ${e.message}")
            null
        }
    }

    /**
     * Atualiza o perfil de imagem no Realm.
     * @param imageData Os dados da imagem de perfil.
     * @param decodedBytes Os bytes decodificados da imagem.
     */
    private fun updateImageProfileInRealm(imageData: ImageProfile, decodedBytes: ByteArray) {
        realm.writeBlocking {
            val existingImageProfile =
                query<ImageProfileRealm>("uuid == $0", imageData.uuid).first().find()
            val imageProfileToUpdate =
                existingImageProfile ?: copyToRealm(formatRealm.toImageProfileImage(imageData)!!)
            imageProfileToUpdate.apply {
                src = decodedBytes
                userUuid = imageData.userUuid
                slot = imageData.slot
            }
        }
    }

    /**
     * Atualiza o estado da lista de chats com a nova imagem de perfil.
     * @param userUuid O UUID do usuário cuja imagem foi atualizada.
     * @param imageData Os dados da imagem.
     * @param decodedBytes Os bytes decodificados da imagem.
     */
    private fun updateChatsStateWithImage(
        userUuid: String,
        imageData: ImageProfile,
        decodedBytes: ByteArray
    ) {
        _chatsResult.value = _chatsResult.value.map { chat ->
            val updatedParticipants = chat.participants.map { participant ->
                if (participant.userId == userUuid && participant.user != null) {
                    val updatedImageProfiles = participant.user.profileImages.map {
                        if (it.uuid == imageData.uuid || it.slot == imageData.slot) {
                            it.copy(src = decodedBytes)
                        } else {
                            it
                        }
                    }.ifEmpty {
                        listOf(
                            ImageProfile(
                                uuid = imageData.uuid,
                                userUuid = userUuid,
                                slot = imageData.slot,
                                src = decodedBytes
                            )
                        )
                    }

                    val updatedUser = participant.user.copy(profileImages = updatedImageProfiles)
                    participant.copy(user = updatedUser)
                } else {
                    participant
                }
            }
            chat.copy(participants = updatedParticipants)
        }
    }

    /**
     * Busca chats no servidor e os insere no banco de dados local.
     * @return A lista de chats obtida do servidor.
     */
    private suspend fun fetchChatsFromServerAndInsert(): List<Chat> {
        return chatRequests.manyRequest(token).also { chats ->
            taskManager.addTask {
                realm.writeBlocking {
                    chats.forEach { chat ->
                        formatRealm.toChat(chat)?.let { copyToRealm(it, UpdatePolicy.ALL) }
                    }
                }
            }
        }
    }

    /**
     * Busca todos os chats no Realm.
     * @return Uma lista de objetos Chat.
     */
    private fun findChatsRealm(): List<Chat> {
        return realm.query<ChatRealm>().find().mapNotNull { it.toRealm() }
    }
}