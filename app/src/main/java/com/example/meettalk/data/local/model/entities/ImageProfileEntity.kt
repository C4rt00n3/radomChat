package com.example.meettalk.data.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "image_perfil",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["uuid"],
            onDelete = ForeignKey.CASCADE, // Exclui imagem ao excluir usuário
            onUpdate = ForeignKey.CASCADE  // Atualiza userUuid se ele mudar
        )
    ]
)
data class ImageProfileEntity(
    @PrimaryKey
    val uuid: String,

    val src: ByteArray?,

    @ColumnInfo(name = "userUuid")
    val userUuid: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageProfileEntity

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

