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
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.remote.ChatEndPoint
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.data.repository.SocketManager
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.example.meettalk.utils.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.query.RealmResults
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class ChatViewModel(private val context: Context, private val realm: Realm) : ViewModel() {
    private val gson = Gson()

    private val _chatsResult = MutableStateFlow<List<Chat>>(listOf())
    val chatsResult: StateFlow<List<Chat>> = _chatsResult

    private val _chatsResultBackup = MutableStateFlow<List<Chat>>(listOf())
    val chatsResultBackup: StateFlow<List<Chat>> = _chatsResultBackup

    private val _message = MutableStateFlow<Message?>(null)
    val message: StateFlow<Message?> = _message

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat

    private val url = context.getText(R.string.baseUrl).toString()

    private val retrofit =
        Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()

    private val apiServiceChat = retrofit.create(ChatEndPoint::class.java)

    private val _isSocketConnected = MutableStateFlow(false)
    val isSocketConnected: StateFlow<Boolean> = _isSocketConnected

    private val _token = MutableStateFlow<String>("")
    val token: StateFlow<String> = _token

    private var socketManager: SocketManager? = null
    private val chatRequests = ChatRequests(url, realm)
    private val format = FormatRealm()
    private val formatR = FormatClass()

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

            data class MessageReady(val chatId: String, val userId: String) {}

            on("messageReady") { args ->
                if (args.isEmpty()) return@on

                val gson = Gson()

                val json = when (val data = args[0]) {
                    is String -> data
                    is JSONObject -> data.toString()
                    else -> {
                        println("Formato de dado inesperado para messageReady.")
                        return@on
                    }
                }

                try {
                    val messageReady = gson.fromJson(json, MessageReady::class.java)
                    println("Recebido messageReady: $messageReady")

                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            realm.write {
                                val chatRealm = query<ChatRealm>("uuid == $0", messageReady.chatId)
                                    .first()
                                    .find()

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
                    }
                } catch (e: Exception) {
                    println("Erro ao processar messageReady: ${e.localizedMessage}")
                }
            }

            on("message") { args ->
                if (args.isNotEmpty()) {
                    val json = when (args[0]) {
                        is String -> args[0] as String
                        is JSONObject -> (args[0] as JSONObject).toString()
                        else -> {
                            return@on
                        }
                    }
                    try {
                        val messageModel = gson.fromJson(json, Message::class.java)
                        viewModelScope.launch(Dispatchers.IO) {
                            realm.write {
                                val chatRealm =
                                    query<ChatRealm>("uuid == $0", messageModel.chatId).first()
                                        .find()
                                val messageRealm = format.toMessageRealm(messageModel)
                                if (chatRealm != null && messageRealm != null) {
                                    chatRealm.messages.add(messageRealm)

                                    copyToRealm(chatRealm, updatePolicy = UpdatePolicy.ALL)
                                }
                            }
                        }
                    } catch (e: JsonSyntaxException) {
                        println("Error parsing JSON: ${e.message}")
                    }
                }
            }

            on("idsChats") { args ->
            }
        }
    }

    fun disconnectSocket() {
        socketManager?.disconnect()
        _isSocketConnected.value = false
    }

    fun setToken(token: String) {
        _token.value = token
    }

    private suspend fun insertChatsData(listChats: List<Chat>) {
        try {
            realm.write {
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

    private suspend fun findChatsData() {
        val chats: RealmResults<ChatRealm> = realm.query<ChatRealm>().find()

        if (chats.isNotEmpty()) chats.map { chat ->
            formatR.fromChatRealm(chat)
        }.let {
            _chatsResult.value = it
            _chatsResultBackup.value = it
        }
    }

    fun observeChats() {
        viewModelScope.launch {
            realm.query<ChatRealm>().asFlow().collect { result ->
                val chats = result.list
                if (chats.isNotEmpty()) {
                    findChatsData()
                }
            }
        }
    }

    fun findMany(token: String) {
        viewModelScope.launch {
            try {
                findChatsData()
                if (_chatsResult.value.isEmpty()) {
                    chatRequests.manyRequest(token).let {
                        insertChatsData(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("Login", "Erro: ${e.message}")
            } finally {
                if (_chatsResult.value.isEmpty()) {
                    findChatsData()
                }
                observeChats()
            }
        }
    }

    fun deleteMany(token: String, list: List<Message>) {
        viewModelScope.launch {
            chatRequests.deleteMany(token, list) { uuids ->
                val currentMessages = _chat.value?.messages.orEmpty()
                val updatedMessages = currentMessages.filterNot { it.uuid in uuids }

                _chat.value = _chat.value?.copy(messages = updatedMessages)
            }
        }
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

    private fun similar(name: String, search: String): Boolean {
        return name.split(" ").any { it.startsWith(search) } // Ex: "rafael" começa com "ra"
    }

    fun constrictedImages(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageLoader = context.imageLoader
            val imagesToUpdate =
                mutableListOf<Pair<String, ByteArray>>()
            realm.query<ImageProfileRealm>().find().map { realmImageProfile ->
                val imageUrl = "$url/image-profile/${realmImageProfile.uuid}"

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .addHeader("Authorization", token)
                    .allowHardware(false)
                    .build()

                async {
                    try {
                        val result = imageLoader.execute(request)

                        if (result.drawable != null) {
                            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

                            bitmap?.let {
                                val stream = ByteArrayOutputStream()
                                it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                val byteArray = stream.toByteArray()
                                imagesToUpdate.add(realmImageProfile.uuid to byteArray)
                            }
                                ?: println("Drawable for ${realmImageProfile.uuid} was not a BitmapDrawable or bitmap was null.")
                        } else {
                            println("No drawable found for ${realmImageProfile.uuid}")
                        }
                    } catch (e: Exception) {
                        println("Error downloading image for ${realmImageProfile.uuid}: ${e.message}")
                        // TODO: Implement more robust error handling (e.g., retry, log to crashlytics)
                    }
                }
            }.awaitAll()

            if (imagesToUpdate.isNotEmpty()) {
                realm.write {
                    imagesToUpdate.forEach { (uuid, byteArray) ->
                        this.query<ImageProfileRealm>("uuid == $0", uuid).first().find()?.apply {
                            src = byteArray
                        }
                    }
                }
            }
        }
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