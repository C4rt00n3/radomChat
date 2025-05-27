package com.example.meettalk.data.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat")
data class ChatEntity(
    @PrimaryKey var uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "createdAt") var createdAt: String,
    @ColumnInfo(name = "lastDateMessage") var lastMessageDate: String?,

    @Ignore
    val messages: List<MessageEntity> = emptyList(),

    @Ignore
    val participants: List<ChatParticipantEntity> = emptyList()
) {
    constructor(uuid: String, createdAt: String, lastMessageDate: String?) : this(
        uuid,
        createdAt,
        lastMessageDate,
        emptyList(),
        emptyList()
    )
}
