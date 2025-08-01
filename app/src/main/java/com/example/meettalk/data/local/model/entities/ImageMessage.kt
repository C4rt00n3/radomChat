package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.ImageMessageRealm
import java.util.UUID

data class ImageMessage(
    val uuid: String,
    val src: ByteArray? = null,
    val user: User? = null,
    val message: Message? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ImageMessage

        if (uuid != other.uuid) return false
        if (!src.contentEquals(other.src)) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + src.contentHashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        return result
    }
}
