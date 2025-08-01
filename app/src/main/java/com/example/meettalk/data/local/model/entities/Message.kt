package com.example.meettalk.data.local.model.entities

import androidx.room.Ignore
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.utils.FormatRealm
import java.time.Instant

data class Message(
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val type: MessageType? = MessageType.TEXT,  // enum MessageType com default TEXT
    val url: String? = null,
    val chatId: String?,
    val createdAt: String,
    val senderId: String,
    val receiverId: String,
    val isRead: Boolean = false,
    val replyToId: String? = null,
    @Ignore
    val isSend: Boolean = true,
    val isUpdate: Boolean = false,
    val updateAt: String? = null,
    val ImageMessage: ImageMessage? = null,
    val countUpdate: Int = 0,
) {
    val format = FormatRealm()
    private fun toMessageRealm(message: Message?): MessageRealm? {
        if (message == null) return null
        return MessageRealm().apply {
            uuid = message.uuid
            text = message.text
            type = message.type?.name ?: MessageType.TEXT.name
            url = message.url
            chatId = message.chatId
            createdAt = message.createdAt
            senderId = message.senderId.toString()
            receiverId = message.receiverId
            isRead = message.isRead
            ImageMessage = message.ImageMessage?.let { format.toImageMessage(it) }
            replyToId = message.replyToId
        }
    }

    fun toRealm() = toMessageRealm(this)
}

