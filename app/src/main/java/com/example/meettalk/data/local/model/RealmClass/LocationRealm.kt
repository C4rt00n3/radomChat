package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.utils.FormatClass
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class LocationRealm : RealmObject {
    @PrimaryKey
    var uuid: String = ""

    var latitude: String? = null
    var longitude: String? = null

    var state: String? = null
    var city: String? = null

    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    var user: UserRealm? = null
    var userId: String? = null

    fun fromLocationRealm() = FormatClass().fromLocationRealm(this)
}