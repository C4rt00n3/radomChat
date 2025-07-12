package com.example.meettalk.data.local.model.entities

data class Block(
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val userId: String? = null,
    val blockedUserId: String,
    val blockedUser: User?
)
