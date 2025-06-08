package com.example.meettalk.data.local.model.RealmClass

import com.example.meettalk.data.local.model.entities.User
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

    var users = realmListOf<UserRealm>()
}