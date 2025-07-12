package com.example.meettalk.utils

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
            isUpdate = messageRealm.isUpdate,
            countUpdate = messageRealm.countUpdate,
            updateAt = messageRealm.updateAt,
            isSend = messageRealm.isSend
        )
    }

    /**
     * Converte um objeto [LocationRealm] (Realm database object) para um objeto [Location] (domain model).
     *
     * @param locationRealm O objeto LocationRealm a ser convertido.
     * @return Um objeto Location com os dados mapeados.
     */
    fun fromLocationRealm(locationRealm: LocationRealm): Location = Location(
        uuid = locationRealm.uuid,
        latitude = locationRealm.latitude,
        longitude = locationRealm.longitude,
        state = State.entries.find { it.name == locationRealm.state },
        city = locationRealm.city,
        createdAt = locationRealm.createdAt,
        updatedAt = locationRealm.updatedAt,
        user = locationRealm.user?.let { fromUserRealm(it) },
        userId = locationRealm.userId
    )

    fun fromPreference(preferenceRealm: PreferenceRealm): Preference {
        return Preference(
            uuid = preferenceRealm.uuid,
            gender = if (preferenceRealm.gender == Gender.M.name) Gender.M else Gender.F,
            maxAge = preferenceRealm.maxAge
        )
    }

    fun fromUserRealm(userRealm: UserRealm): User {
        return User(
            uuid = userRealm.uuid,
            name = userRealm.name,
            gender = enumValueOfOrNull<Gender>(userRealm.gender) ?: Gender.M,
            preferenceUuid = userRealm.preferenceUuid,
            preference = userRealm.preference?.let { fromPreference(it) },
            location = userRealm.location?.let { fromLocationRealm(it) },
            locationId = userRealm.locationId,
            age = userRealm.age,
            profileImages = userRealm.profileImages.map { fromImageProfileRealm(it) },
        )
    }

    fun toPreference(preference: Preference): PreferenceRealm {
        return PreferenceRealm().apply {
            uuid = preference.uuid
            gender = if (preference.gender.name == "M") Gender.M.name else Gender.F.name
            maxAge = preference.maxAge
        }
    }

    fun fromImageProfileRealm(profileImageRealm: ImageProfileRealm): ImageProfile {
        return ImageProfile(
            uuid = profileImageRealm.uuid,
            userUuid = profileImageRealm.userUuid,
            src = profileImageRealm.src,
            isPrimary = profileImageRealm.isPrimary
        )
    }

    fun fromChatRealm(chatRealm: ChatRealm): Chat {
        return Chat(
            uuid = chatRealm.uuid,
            createdAt = chatRealm.createdAt,
            lastMessageDate = chatRealm.lastMessageDate,
            fav = chatRealm.fav,
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

    fun fromBlockreal(blockRealm: BlockRealm): Block = Block(
        uuid = blockRealm.uuid,
        userId = blockRealm.userId,
        blockedUserId = blockRealm.blockedUserId,
        blockedUser = blockRealm.blockedUser?.toClass()
    )

    inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
        return try {
            if (name != null) enumValueOf<T>(name) else null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}