package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.ImageProfileRealm
import java.util.UUID

data class ImageProfile(
    val uuid: String = UUID.randomUUID().toString(),
    val src: ByteArray? = null,
    val userUuid: String? = null,
    val user: User? = null,
    val createAt:String? = null,
    val updateAt:  String? = null,
    val slot: Int = 1
) {
    private fun toImageProfileImage(profileImage: ImageProfile?): ImageProfileRealm? {
        if (profileImage == null) return null
        return ImageProfileRealm().apply {
            uuid = profileImage.uuid
            createAt = profileImage.createAt
            updateAt = profileImage.updateAt
            userUuid = profileImage.userUuid
            src = profileImage.src
            slot = profileImage.slot
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageProfile

        if (uuid != other.uuid) return false
        if (src != null) {
            if (other.src == null) return false
            if (!src.contentEquals(other.src)) return false
        } else if (other.src != null) return false
        if (userUuid != other.userUuid) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + (src?.contentHashCode() ?: 0)
        result = 31 * result + (userUuid?.hashCode() ?: 0)
        result = 31 * result + (user?.hashCode() ?: 0)
        return result
    }

    open fun toRealm() = toImageProfileImage(this)
}

