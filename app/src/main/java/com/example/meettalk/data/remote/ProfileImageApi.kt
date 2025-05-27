package com.example.meettalk.data.remote

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ProfileImageApi {

    @POST("image-profile")
    suspend fun postImage(
        @Header("Authorization") token: String
    ): Response<String>

    @PATCH("image-profile/{uuid}")
    suspend fun updateImage(
        @Header("Authorization") token: String
    ): Response<String>
}
