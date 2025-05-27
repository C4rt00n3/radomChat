package com.example.meettalk.data.local.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "image_message")
data class ImageMessage(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    val src: ByteArray,
    val userUuid: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageMessage

        if (uuid != other.uuid) return false
        if (!src.contentEquals(other.src)) return false
        if (userUuid != other.userUuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + src.contentHashCode()
        result = 31 * result + (userUuid?.hashCode() ?: 0)
        return result
    }
}