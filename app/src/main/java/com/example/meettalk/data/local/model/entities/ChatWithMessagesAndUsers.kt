package com.example.meettalk.data.local.model.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ChatWithMessagesAndUsers(
    @Embedded val chat: ChatEntity,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "chat_uuid"
    )
    val messages: List<MessageEntity>,

    @Relation(
        parentColumn = "uuid", // de ChatEntity
        entity = UserEntity::class,
        entityColumn = "uuid", // de UserEntity
        associateBy = Junction(
            value = ChatUserCrossRef::class,
            parentColumn = "chatId", // DEVE BATER COM O NOME DO CAMPO EM ChatUserCrossRef
            entityColumn = "userId"  // DEVE BATER COM O NOME DO CAMPO EM ChatUserCrossRef
        )
    )
    val users: List<UserEntity>
)
