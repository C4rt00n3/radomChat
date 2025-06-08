package com.example.meettalk.data.local.model.entities

import com.example.meettalk.utils.FormatRealm
import org.jetbrains.annotations.NotNull

data class Chat(
    val uuid: String,
    val createdAt: String,
    val lastMessageDate: String?,
    val messages: List<Message>,
    val participants: List<ChatParticipant>,
)
