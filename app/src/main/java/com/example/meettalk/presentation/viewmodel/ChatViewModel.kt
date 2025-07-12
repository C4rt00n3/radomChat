package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.example.meettalk.R
import com.example.meettalk.data.local.model.RealmClass.BlockRealm
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageMessageRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.LocationRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.data.repository.SocketManager
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TaskManager
import com.example.meettalk.utils.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

open class ChatViewModel() : ViewModel() {
    private val gson = Gson()

    private val _chatsResult = MutableStateFlow<List<Chat>>(listOf())
    open val chatsResult: StateFlow<List<Chat>> = _chatsResult

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

    fun build(context: Context, realm: Realm? = null) {
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
                    PreferenceRealm::class
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
        TokenManager(context).getToken()?.let { this.token = it }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectSocket() {
        val url = url+ "/?token=" + token.replace("Bearer ", "")
        socketManager = SocketManager(url, context)
        socketManager?.connect()

        socketManager?.apply {
            on(Socket.EVENT_CONNECT) {
                _isSocketConnected.value = true
                println("Connected to server")
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                _isSocketConnected.value = false
                println("Disconnected from server")
                if (args.isNotEmpty()) {
                    println("Reason for disconnect: ${args[0]}")
                    if (args[0] is Throwable) {
                        (args[0] as Throwable).printStackTrace()
                    }
                }
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                println("🔁 Reconnect error:")
                args.forEach { println("  ➤ $it") }
                if (args.isNotEmpty() && args[0] is Throwable) {
                    (args[0] as Throwable).printStackTrace()
                }
            }

            on("messageReady") { taskManager.addTask { messageReady(it) } }

            on("message") { args -> taskManager.addTask { onMessageReceived(args) } }

            on("idsChats") {

            }

            on("listMessageRemoved") { taskManager.addTask { listMessageRemoved(it) } }

            on("updateMessage") { taskManager.addTask { updateMessage(it) } }

            on("removeMessages") { taskManager.addTask { removeMessages(it) } }
        }
    }

    private fun removeMessages(args: Array<Any>) {
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

    private suspend fun updateMessage(args: Array<Any>) {
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

    private suspend fun onMessageReceived(args: Array<Any>) {
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
                println("current")
                if (currentChatRealm == null) {
                    val chatApiResult =
                        token.let {
                            messageModel.chatId?.let { it1 ->
                                chatRequests.findOneChat(
                                    it1,
                                    it
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
                        val messageRealm = format.toMessageRealm(messageModel)

                        if (messageRealm != null) {
                            liveChat?.messages?.add(messageRealm)
                            liveChat?.lastMessageDate = messageModel.createdAt
                        } else {
                            println("Erro ao formatar mensagem para Realm: $messageModel")
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

    private suspend fun listMessageRemoved(args: Array<Any>) {
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

    private suspend fun messageReady(args: Array<Any>) {
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

        println(json)
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

    private fun insertChatsData(listChats: List<Chat>) {
        try {
            realm.writeBlocking {
                listChats.forEach { chat ->
                    val chatRealm = format.toChat(chat)
                    if (chatRealm != null) {
                        val formattedParticipants = chatRealm.participants.map {
                            it.id = "${it.userId}${it.chatId}"
                            it
                        }

                        chatRealm.participants = realmListOf(*formattedParticipants.toTypedArray())

                        copyToRealm(chatRealm, updatePolicy = UpdatePolicy.ALL)
                    } else {
                        Log.w("REALM", "ChatRealm nulo para chat: ${chat.uuid}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("REALM", "Falha ao inserir dados do chat", e)
        }
    }

    /**
     * Busca e processa uma lista paginada de objetos ChatRealm do Realm Database.
     *
     * Esta função consulta o Realm para obter uma coleção de chats, aplicando paginação
     * para limitar e compensar os resultados com base nos parâmetros [page] e [size].
     * Os chats recuperados são então formatados e os resultados únicos (baseados no UUID)
     * são atribuídos a [_chatsResult].
     *
     * @param page O número da página a ser recuperada. Padrão para 1.
     * A paginação começa do índice 0 internamente, então a página 1 corresponde
     * aos primeiros [size] itens, a página 2 aos próximos [size] itens, e assim por diante.
     * @param size O número máximo de chats a serem retornados por página. Padrão para 20.
     */
    private fun findChatsData(page: Int = 1, size: Int = 20) {
        val chats = realm
            .query<ChatRealm>()
            .limit(size * page)
            .find()

        if (chats.isNotEmpty()) {
            chats.map { chat ->
                formatR.fromChatRealm(chat)
            }.let { list ->
                _chatsResult.value = list.distinctBy { it.uuid }
            }
        } else {
            _chatsResult.value = emptyList()
        }
    }

    open fun observeChats() {
        viewModelScope.launch {
            realm.query<ChatRealm>().asFlow().collect { result ->
                val chats = result.list
                if (chats.isNotEmpty()) {
                    findChatsData()
                }
            }
        }
    }

    suspend fun findMany(page: Int = 1, size: Int = 20) {
        try {
            findChatsData(page, size)
            var chats = _chatsResult.value
            if (chats.isEmpty()) {
                chats = chatRequests.manyRequest(token)
                insertChatsData(chats.distinctBy { it.uuid })
            }
        } catch (e: Exception) {
            Log.e("Login", "Erro: ${e.message}")
        } finally {
            observeChats()
        }
    }

}