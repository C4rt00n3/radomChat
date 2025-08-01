package com.example.meettalk.data.local.model.entities

import com.example.meettalk.utils.FormatRealm

open class Location(
    var uuid: String,

    var latitude: String?,
    var longitude: String?,

    var state: String?,
    var city: String?,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),

    var user: User? = null,
    var userId: String? = null

){
    fun toRealm() = FormatRealm().toLocation(this)
}



