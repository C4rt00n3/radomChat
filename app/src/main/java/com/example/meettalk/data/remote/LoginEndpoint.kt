package com.example.meettalk.data.remote

import com.example.meettalk.data.local.model.body.LoginRequest
import com.example.meettalk.data.local.model.entities.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginEndpoint {
    @POST("auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("auth/google")
    suspend fun singInGoogle(
        @Header("Authorization") token: String,
    ): Response<AuthResponse>
}