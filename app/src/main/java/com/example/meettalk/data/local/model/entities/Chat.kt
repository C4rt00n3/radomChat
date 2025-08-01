package com.example.meettalk.data.local.model.entities

import com.example.meettalk.utils.FormatRealm
import org.jetbrains.annotations.NotNull

data class Chat(
    val uuid: String,
    val createdAt: String,
    val lastMessageDate: String?,
    val messages: List<Message> = emptyList(),
    val participants: List<ChatParticipant>,
    val fav: Boolean = false
) {
    val format = FormatRealm()

    fun toRealm() = format.toChat(this)
}
