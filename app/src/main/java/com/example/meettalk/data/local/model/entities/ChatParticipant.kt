package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.utils.FormatRealm

data class ChatParticipant(
    val chatId: String,
    val userId: String,
    val chat: Chat? = null,
    val user: User? = null
) {
    private  fun toChatParticipant(chatParticipant: ChatParticipant?): ChatParticipantRealm? {
        if (chatParticipant == null) return null
        return ChatParticipantRealm().apply {
            chatId = chatParticipant.chatId
            userId = chatParticipant.userId
            user = chatParticipant.user?.toRealm()
        }
    }

    fun toRealm() = toChatParticipant(this)
}


