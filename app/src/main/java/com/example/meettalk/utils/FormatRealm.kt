package com.example.meettalk.utils

import android.util.Log
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ChatParticipant
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import io.realm.kotlin.ext.realmListOf
import java.util.UUID

class FormatRealm {

    fun toMessageRealm(message: Message?): MessageRealm? {
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

    fun toUserRealm(user: User?): UserRealm? {
        if (user == null) return null
        return UserRealm().apply {
            uuid = user.uuid
            name = user.name
            gender = user.gender?.name ?: Gender.M.name
            age = user.age
            profileImages = realmListOf(
                *user.profileImages.orEmpty()
                    .mapNotNull { toImageProfileImage(it) }
                    .toTypedArray()
            )
        }
    }

    fun toImageProfileImage(profileImage: ImageProfile?): ImageProfileRealm? {
        if (profileImage == null) return null
        return ImageProfileRealm().apply {
            uuid = profileImage.uuid
            userUuid = profileImage.userUuid
            src = profileImage.src
        }
    }

    fun toChat(chat: Chat?): ChatRealm? {
        if (chat == null) return null

        return try {
            ChatRealm().apply {
                uuid = chat.uuid.ifEmpty { UUID.randomUUID().toString() }
                createdAt = chat.createdAt
                lastMessageDate = chat.lastMessageDate.orEmpty()

                messages = realmListOf<MessageRealm>().apply {
                    chat.messages.forEach { message ->
                        toMessageRealm(message)?.let { add(it) }
                    }
                }

                participants = realmListOf<ChatParticipantRealm>().apply {
                    chat.participants.forEach { participant ->
                        toChatParticipant(participant)?.let { add(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FormatRealm", "Erro ao converter Chat ${chat.uuid}: ${e.message}", e)
            null
        }
    }

    fun toChatParticipant(chatParticipant: ChatParticipant?): ChatParticipantRealm? {
        if (chatParticipant == null) return null
        return ChatParticipantRealm().apply {
            chatId = chatParticipant.chatId
            userId = chatParticipant.userId
            // chat = toChat(chatParticipant.chat) // ❌ Removido para evitar recursividade
            user = toUserRealm(chatParticipant.user)
        }
    }
}
