package com.example.meettalk.data.local.model.entities

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithProfileImages(
    @Embedded val user: UserEntity,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "userUuid",
        entity = ImageProfileEntity::class
    )
    val profileImages: List<ImageProfileEntity>
)