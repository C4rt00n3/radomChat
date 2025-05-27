package com.example.meettalk.data.local.model.entities

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    val user: UserEntity
)