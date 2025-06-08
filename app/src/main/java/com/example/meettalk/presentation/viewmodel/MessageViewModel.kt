package com.example.meettalk.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meettalk.R
import com.example.meettalk.data.local.model.ChatUiState
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.data.remote.ChatRequests
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessageViewModel(
    @SuppressLint("StaticFieldLeak") private val context: Context,
    private val realm: Realm
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat.asStateFlow()

    private val baseUrl = context.getText(R.string.baseUrl).toString()

    private val chatRequests = ChatRequests(baseUrl, realm)
    private val format = FormatRealm()
    private val formatR = FormatClass()

    fun findChat(uuid: String, onSuccess: (Chat) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            realm.write {
                val chatRealm =
                    this.query<ChatRealm>("uuid == $0", uuid).first().find()

                if (chatRealm != null) {
                    onSuccess(formatR.fromChatRealm(chatRealm))
                }
            }
        }
    }

    fun onMessageTextChange(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    fun onStartEditingMessage(message: Message) {
        _uiState.update {
            it.copy(
                messageText = message.text,
                editingMessage = message,
                selectedMessages = listOf(message)
            )
        }
    }

    fun stopEditingMessage() {
        _uiState.update {
            it.copy(
                messageText = "",
                editingMessage = null,
                selectedMessages = listOf()
            )
        }
    }

    fun onStartRespondingToMessage(message: Message) {
        _uiState.update { it.copy(respondingToMessage = message) }
    }

    fun onDismissReply() {
        _uiState.update { it.copy(respondingToMessage = null, editingMessage = null) }
    }

    fun toggleMessageSelection(message: Message) {
        _uiState.update { currentState ->
            val updatedSelection = if (message in currentState.selectedMessages) {
                currentState.selectedMessages - message
            } else {
                currentState.selectedMessages + message
            }
            currentState.copy(selectedMessages = updatedSelection)
        }
    }

    fun toggleSelectAllMyMessages(selected: Boolean, myMessages: List<Message>) {
        _uiState.update {
            it.copy(selectedMessages = if (selected) myMessages else emptyList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createMessage(
        token: String,
        message: CreateMessage,
        error: (Exception) -> Unit = {},
        onSuccess: (Message) -> Unit = {}
    ) {
        viewModelScope.launch {
            chatRequests.create(token, message, error) { message1 ->
                onSuccess(message1)
                _uiState.update { it.copy(messageText = "", respondingToMessage = null) }

                viewModelScope.launch(Dispatchers.IO) {
                    realm.write {
                        val chatRealm =
                            query<ChatRealm>("uuid == $0", message1.chatId).first().find()

                        val messageRealm = format.toMessageRealm(message1)

                        if (chatRealm != null && messageRealm != null) {
                            chatRealm.messages.add(messageRealm)

                            copyToRealm(chatRealm, updatePolicy = UpdatePolicy.ALL)
                        }
                    }
                }
            }
        }
    }

    fun getImageProfileRealm(uuid: String, onSuccess: (ImageProfile) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            realm.write { // Problema 1: 'write' bloqueia e não é o ideal para leitura
                val get =
                    this.query<ImageProfileRealm>("uuid == $0", uuid).first().find()
                val formated = get?.let { formatR.fromImageProfileRealm(it) }

                formated?.let {
                    onSuccess(it)
                }
            }
        }
    }

    fun updateMessage(token: String, message: Message, update: UpdateMessage, chat: Chat) {
        viewModelScope.launch {
            val updatedChatResponse = chatRequests.update(
                token,
                message.uuid,
                update,
                chat
            )

            updatedChatResponse?.let {
                realm.writeBlocking {
                    val messageToUpdate =
                        this.query<MessageRealm>("uuid == $0", message.uuid).first().find()

                    if (update.text?.isNotBlank() == true && messageToUpdate != null) {
                        messageToUpdate.text = update.text
                    }
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        messageText = "",
                        editingMessage = null, // Ensure editing state is cleared
                        selectedMessages = emptyList() // Ensure selected messages are cleared
                    )
                }
            } ?: run {
                println("Error: Failed to update message via network request.")
            }
        }
    }

    fun deleteMessages(token: String, messages: List<Message>) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedSelectedMessages = currentState.selectedMessages.toMutableList().apply {
                    removeAll(messages.toSet())
                }

                currentState.copy(selectedMessages = updatedSelectedMessages)
            }
        }
    }

    fun markChatAsRead(chatUuid: String, token: String, senderId: String) {
        viewModelScope.launch {
            chatRequests.markRead(token = token, chatUuid) {
                viewModelScope.launch(Dispatchers.IO) {
                    realm.writeBlocking {
                        val messagesToUpdate = query<MessageRealm>(
                            "chatId == $0 AND senderId == $1", chatUuid, senderId
                        ).find()
                        messagesToUpdate.forEach { message ->
                            message.isRead = true
                        }
                    }
                }
            }
        }
    }

    fun setChat(chat: Chat) {
        _chat.value = chat
    }

    fun clearMessageSelection() {
        _uiState.update { it.copy(selectedMessages = listOf()) }
    }

    fun clearMessageInput() {
        _uiState.update { it.copy(messageText = "") }
    }

    fun observeChat(uuid: String) {
        viewModelScope.launch {
            realm.query<ChatRealm>().asFlow().collect { result ->
                val chats = result.list
                if (chats.isNotEmpty()) {
                    findChat(uuid) {
                        _chat.value = it
                    }
                }
            }
        }
    }

    fun saveProfileImage(imageProfile: ImageProfile, src: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageProfile = format.toImageProfileImage(imageProfile)
            realm.write {
                val imageGet =
                    this.query<ImageProfileRealm>("uuid == $0", imageProfile?.uuid).first().find()

                if (imageGet != null) {
                    imageGet.src = src
                } else {
                    imageProfile?.let {
                        copyToRealm(it, updatePolicy = UpdatePolicy.ALL)
                    }
                }
            }
        }
    }
}