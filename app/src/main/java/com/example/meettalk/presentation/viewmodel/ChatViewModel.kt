package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.os.Build
import java.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
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
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ContactImageProfileUpdated
import com.example.meettalk.data.local.model.entities.ContactProfileUpdated
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.data.remote.UserRequests
import com.example.meettalk.data.repository.SocketManager
import com.example.meettalk.presentation.components.UiState
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TaskManager
import com.example.meettalk.utils.TokenManager
import com.example.meettalk.utils.getSubFromJwt
import com.example.meettalk.utils.showToast
import com.example.meettalk.utils.users.updateUserInRealm
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ChatViewModel() : ViewModel() {
    private val gson = Gson()

    private val _chatsResult = MutableStateFlow<List<Chat>>(listOf())
    open val chatsResult: StateFlow<List<Chat>> = _chatsResult

    private val _myUser = MutableStateFlow<User?>(null)
    open val myUser: StateFlow<User?> = _myUser

    private lateinit var url: String

    private lateinit var realm: Realm
    private lateinit var retrofit: Retrofit
    private lateinit var apiServiceChat: ChatEndPoint
    private lateinit var context: Context

    private val _isSocketConnected = MutableStateFlow(false)
    open val isSocketConnected: StateFlow<Boolean> = _isSocketConnected

    private val taskManager = TaskManager()
    private lateinit var chatRequests: ChatRequests

    private var socketManager: SocketManager? = null
    private val format = FormatRealm()
    private val formatR = FormatClass()

    private data class MessageReady(val chatId: String, val userId: String) {}
    private data class MessageUUID(val uuid: String) {}

    private lateinit var token: String

    @RequiresApi(Build.VERSION_CODES.O)
    fun build(context: Context, realm: Realm? = null) {
        try {
            val url = context.getText(R.string.baseUrl).toString()
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

                val realm1 by lazy {
                    Realm.open(config)
                }

                this.realm = realm1
            } else {
                this.realm = realm
            }
            this.url = url
            this.context = context
            this.retrofit =
                Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create())
                    .build()
            this.apiServiceChat = this.retrofit.create(ChatEndPoint::class.java)
            this.chatRequests = ChatRequests(url, this.realm)

            val storedToken = TokenManager(context).getToken()

            if (storedToken != null) {
                token = storedToken
            }

            val userUuid = getSubFromJwt(token)

            if (userUuid != null) {
                val user = realm?.query<UserRealm>("uuid == $0", userUuid)?.find()?.firstOrNull()
                user?.toClass()?.let { _myUser.value = it }
            }
            connectSocket()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectSocket() {
        try {
            val url = url + "/?token=" + token.replace("Bearer ", "")
            socketManager = SocketManager(url, context)
            socketManager?.let { manager ->
                com.example.meettalk.data.repository.connectSocket(manager, this@ChatViewModel) {
                    _isSocketConnected.value = it
                }
            }
        } catch (exeption: Exception) {
            exeption.printStackTrace()
        }
    }

    suspend fun newChats(args: Array<Any>) {
        if (args.isEmpty() || args[0] == null) {
            println("Argumentos de mensagem vazios ou nulos.")
            return
        }

        val jsonString = when (args[0]) {
            is String -> args[0] as String
            is JSONObject -> (args[0] as JSONObject).toString()
            else -> {
                println("Argumento de mensagem inválido: tipo ${args[0].javaClass.simpleName}.")
                return
            }
        }

        try {
            data class ChatsPayload(val chats: List<Chat>)

            val chatsPayload = gson.fromJson(jsonString, ChatsPayload::class.java)

            if (chatsPayload?.chats.isNullOrEmpty()) {
                println("Nenhum chat válido encontrado no payload JSON.")
                return
            }

            // Usa write em vez de writeBlocking para operações assíncronas
            realm.write {
                chatsPayload.chats.forEach { chatJson ->
                    // Busca o chat existente pelo UUID
                    val existingChatRealm =
                        query<ChatRealm>("uuid == $0", chatJson.uuid).first().find()

                    if (existingChatRealm == null) {
                        // Chat não existe: converte e copia para o Realm
                        format.toChat(chatJson)?.let { newChatRealm ->
                            copyToRealm(newChatRealm, UpdatePolicy.ALL)
                            println("Novo chat adicionado: ${newChatRealm.uuid}")
                        } ?: println("Falha ao converter chat para Realm: ${chatJson.uuid}")
                    } else {
                        // Chat já existe: atualiza a data da última mensagem e sincroniza mensagens
                        existingChatRealm.apply {
                            lastMessageDate = chatJson.lastMessageDate
                            println("Chat existente atualizado: ${uuid}. Última mensagem data: ${lastMessageDate}")

                            // Sincroniza mensagens: Adiciona novas mensagens ou atualiza existentes
                            chatJson.messages.forEach { messageJson ->
                                // Busca a mensagem existente no chat Realm
                                val existingMessageRealm =
                                    messages.query<MessageRealm>("uuid == $0", messageJson.uuid)
                                        .first().find()

                                if (existingMessageRealm == null) {
                                    // Mensagem não existe: adiciona ao RealmList do chat
                                    messageJson.toRealm()?.let { newMessageRealm ->
                                        messages.add(newMessageRealm)
                                        println("Mensagem nova adicionada ao chat ${uuid}: ${newMessageRealm.uuid}")
                                    }
                                        ?: println("Falha ao converter mensagem para Realm: ${messageJson.uuid}")
                                } else {
                                    // Mensagem existe: atualiza seus campos se necessário.
                                    // Se suas mensagens tiverem campos mutáveis ou que precisam de atualização.
                                    // Ex: existingMessageRealm.content = messageJson.content
                                    println("Mensagem existente atualizada/verificada no chat ${uuid}: ${existingMessageRealm.uuid}")
                                }
                            }
                            // Opcional: Remover mensagens que não estão mais no payload de entrada.
                            // Isso requer uma lógica mais complexa de diff, caso seja necessário.
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Loga o erro para facilitar a depuração
            System.err.println("Erro ao processar novos chats: ${e.message}")
            e.printStackTrace() // Imprime o stack trace completo para depuração
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun contactImageProfileUpdated(args: Array<Any>) {
        if (args.isEmpty()) {
            println("Argumentos de mensagem vazios.")
            return
        }

        val jsonString = when (args[0]) {
            is String -> args[0] as String
            is JSONObject -> (args[0] as JSONObject).toString()
            else -> {
                println("Argumento de mensagem inválido: ${args[0].javaClass.simpleName}")
                return
            }
        }

        try {
            gson.fromJson(jsonString, ContactImageProfileUpdated::class.java)?.let { res ->

                val decodedBytes: ByteArray? = res.data.src.let { base64String ->
                    try {
                        Base64.getDecoder().decode(base64String)
                    } catch (e: IllegalArgumentException) {
                        println("Erro ao decodificar Base64: ${e.message}")
                        null
                    }
                }

                realm.writeBlocking {
                    val existingImageProfile = query<ImageProfileRealm>("uuid == $0", res.data.uuid)
                        .find()
                        .firstOrNull()

                    val imageProfileToUpdate =
                        existingImageProfile ?: copyToRealm(ImageProfileRealm().apply {
                            uuid = res.data.uuid
                            userUuid = res.data.userUuid
                            slot = res.data.slot
                        })

                    imageProfileToUpdate.src = decodedBytes
                    imageProfileToUpdate.userUuid = res.data.userUuid
                    imageProfileToUpdate.slot = res.data.slot
                }

                _chatsResult.value = _chatsResult.value.map { chat ->
                    val updatedParticipants = chat.participants.map { participant ->
                        if (participant.userId == res.data.userUuid && participant.user != null) {
                            val updatedImageProfiles = participant.user.profileImages.map {
                                if (it.uuid == res.data.uuid || it.slot == res.data.slot) {
                                    it.copy(src = decodedBytes)
                                } else {
                                    it
                                }
                            }.ifEmpty {
                                listOf(
                                    ImageProfile(
                                        uuid = res.data.uuid,
                                        userUuid = res.data.userUuid,
                                        slot = res.data.slot,
                                        src = decodedBytes
                                    )
                                )
                            }

                            val updatedUser =
                                participant.user.copy(profileImages = updatedImageProfiles)
                            participant.copy(user = updatedUser)
                        } else {
                            participant
                        }
                    }
                    chat.copy(participants = updatedParticipants)
                }
            }
        } catch (error: Exception) {
            println("Erro ao processar mensagem de atualização de perfil de imagem: $error")
        }
    }

    fun contactProfileUpdated(args: Array<Any>) {
        if (args.isEmpty()) {
            println("Argumentos de mensagem vazios.")
            return
        }

        val jsonString = when (args[0]) {
            is String -> args[0] as String
            is JSONObject -> (args[0] as JSONObject).toString()
            else -> {
                println("Argumento de mensagem inválido: ${args[0].javaClass.simpleName}")
                return
            }
        }

        try {
            val data = gson.fromJson(jsonString, ContactProfileUpdated::class.java)
            updateUserInRealm(realm, data.userId, data.data)
        } catch (error: Exception) {
            println(error)
            showToast("Error ao atualizar usuario", context)
        }

    }

    fun removeMessages(args: Array<Any>) {
        if (args.isEmpty()) return

        val gson = Gson()

        val json = when (val data = args[0]) {
            is String -> data
            is JSONObject -> data.toString()
            is JSONArray -> data.toString()
            else -> {
                println("Formato de dado inesperado para removeMessages. Tipo recebido: ${data::class.java.simpleName}, Valor: $data")
                return
            }
        }

        try {
            val listType = object : TypeToken<List<MessageUUID>>() {}.type
            val messages: List<MessageUUID> = gson.fromJson(json, listType)

            if (messages.isNotEmpty()) {
                realm.writeBlocking {
                    val messagesToDelete =
                        this.query<MessageRealm>("uuid IN $0", messages.map { it.uuid }).find()

                    delete(messagesToDelete)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateMessage(args: Array<Any>) {
        if (args.isEmpty()) {
            println("Evento 'updateMessage' recebido com argumentos vazios.")
            return
        }
        val gson = Gson()
        val json = when (val data = args[0]) {
            is String -> data
            is JSONObject -> data.toString()
            else -> {
                println("Formato de dado inesperado para 'updateMessage'. Tipo recebido: ${data::class.java.simpleName}, Valor: $data")
                return
            }
        }

        try {
            val incomingMessage = gson.fromJson(json, Message::class.java)
            val messageRealmToUpdate =
                realm.query<MessageRealm>("uuid == $0", incomingMessage.uuid).find().firstOrNull()
            if (messageRealmToUpdate != null) {
                realm.write {
                    findLatest(messageRealmToUpdate)?.text = incomingMessage.text
                    println("Mensagem Realm ${incomingMessage.uuid} atualizada com o texto: '${incomingMessage.text}'")
                }
            } else {
                println("Mensagem com UUID ${incomingMessage.uuid} não encontrada no Realm para atualização.")
            }
        } catch (e: Exception) {
            System.err.println("Erro ao processar evento 'updateMessage': ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun onMessageReceived(args: Array<Any>) {
        if (args.isEmpty()) {
            println("Argumentos de mensagem vazios.")
            return
        }

        val jsonString = when (args[0]) {
            is String -> args[0] as String
            is JSONObject -> (args[0] as JSONObject).toString()
            else -> {
                println("Argumento de mensagem inválido: ${args[0].javaClass.simpleName}")
                return
            }
        }

        try {
            val messageModel = gson.fromJson(jsonString, Message::class.java).copy(isSend = true)
            try {
                var currentChatRealm =
                    realm.query<ChatRealm>("uuid == $0", messageModel.chatId).first().find()

                if (currentChatRealm == null) {
                    val chatApiResult = token.let {
                        messageModel.chatId?.let { it1 ->
                            chatRequests.findOneChat(
                                it1, it
                            )
                        }
                    }

                    chatApiResult?.let { apiChat ->
                        format.toChat(apiChat)?.let { newChatRealm ->
                            realm.write {
                                currentChatRealm = copyToRealm(
                                    newChatRealm, updatePolicy = UpdatePolicy.ALL
                                )
                            }
                        } ?: println("Erro: Chat formatado para Realm é nulo.")
                    } ?: println("Erro: Resultado da API para chat é nulo.")
                }

                currentChatRealm?.let { chat ->
                    realm.write {
                        val liveChat = findLatest(chat)
                        val messageModelCopy =
                            messageModel.copy()

                        var imageRealmToAttach: ImageMessageRealm? = null
                        messageModelCopy.ImageMessage?.let {
                            val tempImageRealm =
                                FormatRealm().toImageMessage(it)

                            imageRealmToAttach = copyToRealm(tempImageRealm, UpdatePolicy.ALL)
                        }

                        val messageRealm = format.toMessageRealm(messageModelCopy)

                        if (messageRealm != null) {

                            messageRealm.ImageMessage = imageRealmToAttach

                            liveChat?.messages?.add(messageRealm)
                            liveChat?.lastMessageDate = messageModel.createdAt
                        } else {
                            println("Erro ao formatar mensagem para Realm: $messageModelCopy")
                        }
                    }
                }
                    ?: println("Erro: Chat não encontrado ou não pôde ser criado para adicionar mensagem.")

            } catch (e: Exception) {
                println("Erro ao processar mensagem no Realm ou API: ${e.message}")
                e.printStackTrace()
            }
        } catch (e: JsonSyntaxException) {
            println("Erro de sintaxe JSON ao analisar mensagem: ${e.message}. JSON: $jsonString")
        } catch (e: Exception) {
            println("Ocorreu um erro inesperado ao processar a mensagem: ${e.message}. JSON: $jsonString")
        }
    }

    suspend fun listMessageRemoved(args: Array<Any>) {
        if (args.isNotEmpty()) {
            val jsonString = when (args[0]) {
                is String -> args[0] as String
                is JSONObject -> (args[0] as JSONObject).toString()
                else -> {
                    println("Argumento de mensagem inválido: ${args[0].javaClass.simpleName}")
                    return
                }
            }

            val gson = Gson()
            val type = object : TypeToken<List<String>>() {}.type
            val removedMessageUuids: List<String> = gson.fromJson(jsonString, type)

            if (removedMessageUuids.isNotEmpty()) {
                realm.write {
                    val messagesToDelete =
                        this.query<MessageRealm>("uuid IN $0", removedMessageUuids).find()
                    delete(messagesToDelete)
                }
            }
        }
    }

    suspend fun messageReady(args: Array<Any>) {
        if (args.isEmpty()) return

        val gson = Gson()

        val json = when (val data = args[0]) {
            is String -> data
            is JSONObject -> data.toString()
            else -> {
                println("Formato de dado inesperado para messageReady.")
                return
            }
        }

        try {
            val messageReady = gson.fromJson(json, MessageReady::class.java)
            try {
                realm.write {
                    val chatRealm =
                        query<ChatRealm>("uuid == $0", messageReady.chatId).first().find()
                    if (chatRealm == null) {
                        println("Chat com ID ${messageReady.chatId} não encontrado.")
                        return@write
                    }

                    chatRealm.messages.forEach { message ->
                        if (message.senderId == messageReady.userId) {
                            message.isRead = true
                        }
                    }

                    println("Mensagens marcadas como lidas para chat ${messageReady.chatId}")
                }
            } catch (e: Exception) {
                println("Erro ao marcar mensagens como lidas: ${e.localizedMessage}")
            }
        } catch (e: Exception) {
            println("Erro ao processar messageReady: ${e.localizedMessage}")
        }
    }

    suspend fun findChatsServer(): List<Chat> {
        return chatRequests.manyRequest(token)
    }

    private fun insertInBank(chat: Chat?) {
        val format = FormatRealm()
        realm.writeBlocking {
            format.toChat(chat)?.let {
                copyToRealm(it, UpdatePolicy.ALL)
            }
        }
    }

    private suspend fun findChatsRealm(): List<Chat> {
        val chats = realm.query<ChatRealm>().find()
        return chats.mapNotNull {
            it.toRealm()
        }
    }

    suspend fun findChats() {
        _chatsResult.value = findChatsRealm().let { chatsRealm ->
            chatsRealm.ifEmpty {
                findChatsServer().let { chats ->
                    taskManager.addTask { chats.forEach { insertInBank(it) } }
                    chats
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun observeChats() {
        viewModelScope.launch {
            realm.query<ChatRealm>().asFlow().collect { result ->
                val chats = result.list
                if (chats.isNotEmpty()) {
                    findChatsRealm()
                }
            }
        }
    }
}