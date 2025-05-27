package com.example.meettalk.data.local.model.body

import com.example.meettalk.data.local.model.body.enums.MessageType


class CreateMessage (
    val text: String,
    val type: MessageType = MessageType.TEXT,
    val url: String? = null,
    val receiverId: String,
    val replyToId: String? = null
)