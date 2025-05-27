package com.example.meettalk.data.local.model.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "block",
    indices = [Index(value = ["userId", "blockedUserId"], unique = true)]
)
data class BlockEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val blockedUserId: String
)