package com.example.meettalk.data.local.model.body

import com.example.meettalk.data.local.model.body.enums.MessageType


class CreateMessage (
    val uuid: String? = null,
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val url: String? = null,
    val receiverId: String,
    val senderId: String,
    val replyToId: String? = null,
    val createdAt: String? = null
) {
}