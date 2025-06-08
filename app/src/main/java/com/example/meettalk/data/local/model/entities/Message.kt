package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.utils.FormatRealm
import java.time.Instant

data class Message(
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val type: MessageType? = MessageType.TEXT,  // enum MessageType com default TEXT
    val url: String? = null,
    val chatId: String,
    val createdAt: String,
    val senderId: String,
    val receiverId: String? = null,
    val isRead: Boolean = false,
    val replyToId: String? = null,
) {
    private fun toMessageRealm(message: Message?): MessageRealm? {
        if (message == null) return null
        return MessageRealm().apply {
            uuid = message.uuid
            text = message.text
            type = message.type?.name ?: MessageType.TEXT.name
            url = message.url
            chatId = message.chatId
            createdAt = message.createdAt
            senderId = message.senderId
            receiverId = message.receiverId
            isRead = message.isRead
            replyToId = message.replyToId
        }
    }

    fun toRealm() = toMessageRealm(this)
}

