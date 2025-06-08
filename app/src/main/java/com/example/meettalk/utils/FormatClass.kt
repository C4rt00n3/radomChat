package com.example.meettalk.utils

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

class FormatClass {
    fun fromMessageRealm(messageRealm: MessageRealm): Message {
        return Message(
            uuid = messageRealm.uuid,
            text = messageRealm.text,
            type = enumValueOfOrNull<MessageType>(messageRealm.type) ?: MessageType.TEXT,
            url = messageRealm.url,
            chatId = messageRealm.chatId,
            createdAt = messageRealm.createdAt,
            senderId = messageRealm.senderId,
            receiverId = messageRealm.receiverId,
            isRead = messageRealm.isRead,
            replyToId = messageRealm.replyToId,
        )
    }

    fun fromUserRealm(userRealm: UserRealm): User {
        return User(
            uuid = userRealm.uuid,
            name = userRealm.name,
            gender = enumValueOfOrNull<Gender>(userRealm.gender) ?: Gender.M,
            age = userRealm.age,
            profileImages = userRealm.profileImages.map { fromImageProfileRealm(it) },
//            chatParticipants = userRealm.chatParticipants.map { it.toClass() },
        )
    }

    fun fromImageProfileRealm(profileImageRealm: ImageProfileRealm): ImageProfile {
        return ImageProfile(
            uuid = profileImageRealm.uuid,
            userUuid = profileImageRealm.userUuid,
            src = profileImageRealm.src
        )
    }

    fun fromChatRealm(chatRealm: ChatRealm): Chat {
        return Chat(
            uuid = chatRealm.uuid,
            createdAt = chatRealm.createdAt,
            lastMessageDate = chatRealm.lastMessageDate,
            messages = chatRealm.messages.map { fromMessageRealm(it) },
            participants = chatRealm.participants.map { fromChatParticipantRealm(it) }
        )
    }

    fun fromChatParticipantRealm(participantRealm: ChatParticipantRealm): ChatParticipant {
        return ChatParticipant(
            chatId = participantRealm.chatId,
            userId = participantRealm.userId,
            chat = if (participantRealm.chat != null) fromChatRealm(participantRealm.chat!!) else null,
            user = if (participantRealm.user != null) fromUserRealm(participantRealm.user!!) else null
        )
    }

    // Helper function to safely convert strings to enum (nullable)
    inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
        return try {
            if (name != null) enumValueOf<T>(name) else null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}