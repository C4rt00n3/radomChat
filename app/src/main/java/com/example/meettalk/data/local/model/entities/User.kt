package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.RealmClass.UserRealm
import com.example.meettalk.data.local.model.body.enums.Gender
import com.example.meettalk.utils.FormatRealm
import io.realm.kotlin.ext.realmListOf


data class User(
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val gender: Gender? = Gender.M,          // assumindo enum Gender com M como default
    val chatParticipants: List<ChatParticipant> = emptyList(),
    val profileImages: List<ImageProfile> = emptyList(),
) {
    private fun toUserRealm(user: User?): UserRealm? {
        if (user == null) return null
        return UserRealm().apply {
            uuid = user.uuid
            name = user.name
            gender = user.gender?.name ?: Gender.M.name
            age = user.age
            profileImages = realmListOf(
                *user.profileImages.orEmpty()
                    .mapNotNull { it.toRealm() }
                    .toTypedArray()
            )
        }
    }

    fun toRealm() = toUserRealm(this)
}

