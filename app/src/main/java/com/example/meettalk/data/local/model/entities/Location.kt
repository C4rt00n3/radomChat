package com.example.meettalk.data.local.model.entities

import com.example.meettalk.data.local.model.body.enums.State

open class Location(
    var uuid: String,

    var latitude: String?,
    var longitude: String?,

    var state: State?,
    var city: String?,

    var users: List<User>
)



