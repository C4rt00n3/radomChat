package com.example.meettalk.presentation.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.meettalk.R
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.DeleteMessageBody
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.data.local.model.entities.ChatUserCrossRef
import com.example.meettalk.data.local.model.entities.MessageEntity
import com.example.meettalk.data.local.model.entities.UserEntity
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.repository.SocketManager
import com.example.meettalk.presentation.databeses.ChatDatabase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class ChatViewModel(context: Context) : ViewModel() {
    private val gson = Gson()

    private val db = Room.databaseBuilder(
        context.applicationContext,
        ChatDatabase::class.java,
        "chat_database"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val _chatsResult = MutableStateFlow<List<ChatEntity>>(listOf())
    val chatsResult: StateFlow<List<ChatEntity>> = _chatsResult

    private val _chatsResultBackup = MutableStateFlow<List<ChatEntity>>(listOf())
    val chatsResultBackup: StateFlow<List<ChatEntity>> = _chatsResultBackup

    private val _message = MutableStateFlow<MessageEntity?>(null)
    val message: StateFlow<MessageEntity?> = _message

    private val _chat = MutableStateFlow<ChatEntity?>(null)
    val chat: StateFlow<ChatEntity?> = _chat

    private val url = context.getText(R.string.baseUrl).toString()

    private val retrofit =
        Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()

    private val apiServiceChat = retrofit.create(ChatEndPoint::class.java)

    private val _isSocketConnected = MutableStateFlow(false)
    val isSocketConnected: StateFlow<Boolean> = _isSocketConnected

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private var socketManager: SocketManager? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectSocket(baseUrl: String, context: Context) {
        socketManager = SocketManager(baseUrl, context)
        socketManager?.connect()

        socketManager?.apply {
            on(Socket.EVENT_CONNECT) {
                _isSocketConnected.value = true
                println("Connected to server")
            }

            on(Socket.EVENT_DISCONNECT) {
                _isSocketConnected.value = false
                println("Disconnected from server")
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                println("🔁 Reconnect error:")
                args.forEach { println("  ➤ $it") }
                if (args.isNotEmpty() && args[0] is Throwable) {
                    (args[0] as Throwable).printStackTrace()
                }
            }

            data class MessageReady(val chatId: String) {}

            on("messageReady") { args ->
                if (args.isEmpty()) return@on

                val json = when (val data = args[0]) {
                    is String -> data
                    is JSONObject -> data.toString()
                    else -> return@on
                }

                try {
                    val messageReady = gson.fromJson(json, MessageReady::class.java)
                    _chatsResult.value = _chatsResult.value.map { chat ->
                        if (chat.uuid == messageReady.chatId) {
                            chat.copy(messages = chat.messages.map { message ->
                                if (message.senderId == user.value?.uuid)
                                    message.copy(isRead = true)
                                else message
                            })
                        } else chat
                    }

                    if (_chat.value?.uuid == messageReady.chatId) {
                        _chat.value = _chatsResult.value.find { it.uuid == _chat.value?.uuid }
                    }
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }

            on("message") { args ->
                if (args.isNotEmpty()) {
                    val json = when (args[0]) {
                        is String -> args[0] as String
                        is JSONObject -> (args[0] as JSONObject).toString()
                        else -> {
                            println("Unexpected type: ${args[0]::class.java.simpleName}")
                            return@on
                        }
                    }
                    try {
                        val messageModel = gson.fromJson(json, MessageEntity::class.java)
                        addMessage(messageModel)
                    } catch (e: JsonSyntaxException) {
                        println("Error parsing JSON: ${e.message}")
                    }
                }
            }

            on("idsChats") { args ->
                // Handle other socket events as needed
            }
        }
    }

    fun disconnectSocket() {
        socketManager?.disconnect()
        _isSocketConnected.value = false
    }

    fun setUser(user: UserEntity) {
        _user.value = user
    }

    fun sicroninedGet() {
        viewModelScope.launch {

        }
    }

    fun findMany(token: String) {
        viewModelScope.launch {
            try {
                val response: Response<List<ChatEntity>> = apiServiceChat.findAll(token)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _chatsResult.value = it
                        _chatsResultBackup.value = it
                    }
                } else {
                    _chatsResult.value = listOf()
                }
            } catch (e: Exception) {
                _chatsResult.value = listOf()
                Log.e("Login", "Erro: ${e.message}")
            }
            finally {
                sicronize()
            }
        }
    }

    private suspend fun createChatWithUsers(chat: ChatEntity, users: List<UserEntity>) {
        db.chatDao().insertChat(chat)
        db.userDao().insertUsers(users)

        val profileImages = users.flatMap { it.profileImages }

        if (profileImages.isNotEmpty()) {
            db.userDao().insertProfileImages(profileImages)
        }

        val crossRefs = users.map { user ->
            ChatUserCrossRef(
                chatId = chat.uuid,
                userId = user.uuid
            )
        }

        db.userDao().insertChatUserCrossRefs(crossRefs)
    }

    fun sicronize() {
        val chats = _chatsResult.value

        val users = chats.flatMap { chat ->
            chat.participants.map { it.user }
        }.distinctBy { it?.uuid ?: "" }.toMutableList()

        val locations = users.map { it?.location }.mapNotNull { it }
        val messages: List<MessageEntity> = chats.flatMap { it.messages }

        viewModelScope.launch {
            chats.map {
                createChatWithUsers(it, it.participants.mapNotNull { it.user })
            }
            db.locationDao().insertLocations(locations)
            db.messageDao().insertMessages(messages)
        }
    }

    fun deleteMany(token: String, list: List<MessageEntity>) {
        viewModelScope.launch {
            try {
                val uuids = list.map { it.uuid }
                val body = DeleteMessageBody(uuids)

                val response = apiServiceChat.delete(token, body)

                if (response.isSuccessful) {
                    val currentMessages = _chat.value?.messages.orEmpty()
                    val updatedMessages = currentMessages.filterNot { it.uuid in uuids }

                    _chat.value = _chat.value?.copy(messages = updatedMessages)
                } else {
                    Log.e("Chat", "Erro ao deletar mensagens: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Chat", "Exceção ao deletar mensagens: ${e.message}", e)
            }
        }
    }

    fun markRead(chatId: String, token: String, myUser: UserEntity) {
        viewModelScope.launch {
            try {
                val response = apiServiceChat.markRead(token, chatId)
                if (response.isSuccessful) {
                    val updatedMessages = _chat.value?.messages?.map {
                        if (it.senderId != myUser.uuid) it.copy(isRead = true) else it
                    } ?: listOf()

                    _chat.value = _chat.value?.copy(messages = updatedMessages)
                } else {
                    println(response.errorBody()?.string())
                }
            } catch (error: Exception) {
                Log.e("Login", "Erro: ${error.message}")
            }
        }
    }

    fun find(uuid: UUID, token: String) {
        viewModelScope.launch {
            try {
                val response = apiServiceChat.find(token, uuid)
                if (response.isSuccessful) {
                    _message.value = response.body()
                } else {
                    _message.value = null
                }
            } catch (e: Exception) {
                _message.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun create(token: String, data: CreateMessage, onSuccess: (MessageEntity) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = apiServiceChat.create(token, data)
                if (response.isSuccessful) {
                    val newMessage = response.body()

                    newMessage?.let { message ->
                        val updatedChats = _chatsResult.value.map { chat ->
                            if (chat.uuid == message.chatId) {
                                val updatedMessages = chat.messages.toMutableList()
                                updatedMessages.addFirst(message)

                                val updatedChat = chat.copy(messages = updatedMessages)

                                if (_chat.value?.uuid == updatedChat.uuid) {
                                    _chat.value = updatedChat
                                }

                                updatedChat
                            } else {
                                chat
                            }
                        }

                        _chatsResult.value = updatedChats
                        onSuccess(message)
                    }
                } else {
                    Log.d("Error", "Erro na resposta: ${response.code()}")
                    println(response.errorBody()?.string())
                }
            } catch (error: Exception) {
                Log.d("Error", error.message.toString())
                error.printStackTrace()
            }
        }
    }

    fun update(token: String, id: String, data: UpdateMessage) {
        viewModelScope.launch {
            try {
                val response = apiServiceChat.update(token, id, data)
                if (response.isSuccessful) {
                    val novaMessage = response.body()
                    novaMessage?.let { message ->
                        _chat.value?.let { chatAtual ->
                            val novoChat =
                                chatAtual.copy(messages = chatAtual.messages.map { msg ->
                                    if (msg.uuid == message.uuid) message else msg
                                })
                            _chat.value = novoChat
                        }
                    }
                }
            } catch (error: Exception) {
                println(error)
                Log.d("Error", error.message.toString())
            }
        }
    }

    fun setChat(chat: ChatEntity) {
        _chat.value = chat
    }

    fun search(text: String) {
        val normalizedSearch = text.trim().lowercase()
        _chatsResult.value = _chatsResultBackup.value.filter { chat ->
            chat.participants.any { participant ->
                participant.user?.name?.trim()?.lowercase()?.let { nome ->
                    nome.contains(normalizedSearch) || similar(nome, normalizedSearch)
                } == true
            }
        }
    }

    fun resetChats() {
        _chatsResult.value = _chatsResultBackup.value
    }

    fun findChat(uuid: String): ChatEntity? {
        return _chatsResult.value.find { it.uuid == uuid }
    }

    fun addMessage(message: MessageEntity) {
        val currentChats = _chatsResult.value
        val chat = currentChats.find { it.uuid == message.chatId } ?: return

        val exists = chat.messages.any { it.uuid == message.uuid }
        if (exists) return

        val updatedMessages = chat.messages.reversed() + message
        val updatedChat = chat.copy(messages = updatedMessages.reversed())

        _chatsResult.value = currentChats.map {
            if (it.uuid == chat.uuid) updatedChat else it
        }

        if (_chat.value?.uuid == message.chatId) {
            _chat.value = updatedChat
        }

        val verifyChat = _chatsResult.value.any {
            it.uuid == message.chatId
        }

        if (verifyChat)
            _chat.value = updatedChat
    }

    private fun similar(name: String, search: String): Boolean {
        return name.split(" ").any { it.startsWith(search) } // Ex: "rafael" começa com "ra"
    }

    suspend fun verifyToken(token: String): Boolean {
        return try {
            val response = apiServiceChat.verifyToken(token)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
