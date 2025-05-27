package com.example.meettalk.data.local.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.meettalk.data.local.model.body.enums.Gender

@Entity(
    tableName = "usuario",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class UserEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val age: Int,
    val gender: Gender,
    val locationId: String?
) {
    @Ignore
    var location: LocationEntity? = null

    @Ignore
    var profileImages: List<ImageProfileEntity> = listOf()

    constructor(
        uuid: String,
        name: String,
        age: Int,
        gender: Gender,
        locationId: String?,
        location: LocationEntity?,
        profileImages: List<ImageProfileEntity>
    ) : this(uuid, name, age, gender, locationId) {
        this.location = location
        this.profileImages = profileImages
    }
}

