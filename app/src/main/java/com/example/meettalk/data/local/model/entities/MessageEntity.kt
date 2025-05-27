package com.example.meettalk.data.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.meettalk.data.local.model.body.enums.MessageType
import java.util.UUID

@Entity(
    tableName = "message",
    foreignKeys = [ForeignKey(
        UserEntity::class,
        parentColumns = ["uuid"],
        childColumns = ["sender_id"]
    ), ForeignKey(
        UserEntity::class,
        parentColumns = ["uuid"],
        childColumns = ["receive_id"]
    ),
        ForeignKey(
            ChatEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["chat_uuid"]
        )],
)
data class MessageEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val text: String,
    @ColumnInfo(name = "type") val type: MessageType = MessageType.TEXT,
    val url: String? = null,
    @ColumnInfo(name = "chat_uuid") val chatId: String,
    @ColumnInfo(name = "createdAt") val createdAt: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "receive_id") val receiverId: String? = null,
    val replyToId: String? = null,
    val isRead: Boolean = false
)