package com.example.meettalk.data.local.model.entities

data class ImageMessage(
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val src: ByteArray?,
    val userUuid: String? = null,
    val user: User? = null
)
