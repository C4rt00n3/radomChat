package com.example.meettalk.utils

import android.util.Log
import com.example.meettalk.data.local.model.RealmClass.BlockRealm
import com.example.meettalk.data.local.model.RealmClass.ChatParticipantRealm
import com.example.meettalk.data.local.model.RealmClass.ChatRealm
import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import com.example.meettalk.data.local.model.RealmClass.LocationRealm
import com.example.meettalk.data.local.model.RealmClass.MessageRealm
import com.example.meettalk.data.local.model.RealmClass.PreferenceRealm
import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.data.local.model.body.enums.MessageType
import com.example.meettalk.data.local.model.body.enums.State
import com.example.meettalk.data.local.model.entities.Block
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.ChatParticipant
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.Location
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.Preference
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
            isUpdate = message.isUpdate
            updateAt = message.updateAt
            countUpdate = message.countUpdate
            isSend = message.isSend
        }
    }

    fun toBlock(block: Block): BlockRealm =
        BlockRealm().apply {
            uuid = block.uuid
            userId = block.userId
            blockedUserId = block.blockedUserId
            blockedUser = block.blockedUser?.toRealm()
        }

    fun toUserRealm(user: User?): UserRealm? {
        if (user == null) return null
        return UserRealm().apply {
            uuid = user.uuid
            name = user.name
            gender = user.gender?.name ?: Gender.M.name
            age = user.age
            location = user.location?.let { toLocation(it) }
            locationId = user.locationId
            profileImages = realmListOf(
                *user.profileImages.orEmpty()
                    .mapNotNull { toImageProfileImage(it) }
                    .toTypedArray()
            )
            preference = user.preference?.let { toPreference(it) }
            preferenceUuid = user.preferenceUuid
        }
    }

    fun toImageProfileImage(profileImage: ImageProfile?): ImageProfileRealm? {
        if (profileImage == null) return null
        return ImageProfileRealm().apply {
            uuid = profileImage.uuid
            userUuid = profileImage.userUuid
            src = profileImage.src
            isPrimary = profileImage.isPrimary
        }
    }

    fun toChat(chat: Chat?): ChatRealm? {
        if (chat == null) return null

        return try {
            ChatRealm().apply {
                uuid = chat.uuid.ifEmpty { UUID.randomUUID().toString() }
                createdAt = chat.createdAt
                lastMessageDate = chat.lastMessageDate.orEmpty()
                fav = chat.fav
                messages = realmListOf<MessageRealm>().apply {
                    (chat.messages ?: emptyList()).forEach { message ->
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

    fun toPreference(preferenceRealm: Preference): PreferenceRealm {
        return PreferenceRealm().apply {
            uuid = preferenceRealm.uuid
            gender = preferenceRealm.gender.name
            maxAge = preferenceRealm.maxAge
        }
    }

    fun toLocation(location: Location): LocationRealm {
        return LocationRealm().apply {
            uuid = location.uuid
            latitude = location.latitude
            longitude = location.longitude
            state = location.state?.name
            city = location.city
            createdAt = location.createdAt
            updatedAt = location.updatedAt
            user = toUserRealm(location.user)
            userId = location.userId
        }
    }

    private fun toChatParticipant(chatParticipant: ChatParticipant?): ChatParticipantRealm? {
        if (chatParticipant == null) return null
        return ChatParticipantRealm().apply {
            chatId = chatParticipant.chatId
            userId = chatParticipant.userId
            user = toUserRealm(chatParticipant.user)
        }
    }
}
