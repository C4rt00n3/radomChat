package com.example.meettalk.data.local.model.entities

import java.util.UUID

data class Auth(
    val uuid: String = UUID.randomUUID().toString(),
    val email: String,
    val password: String
)