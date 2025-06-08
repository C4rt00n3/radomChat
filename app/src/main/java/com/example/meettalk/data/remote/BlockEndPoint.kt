package com.example.meettalk.data.remote

import com.example.meettalk.data.local.model.entities.Block
import com.example.meettalk.data.local.model.entities.Message
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BlockEndPoint {
    @POST("block/{user_id}")
    suspend fun create(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<Block>

    @DELETE("block/{userId}")
    suspend fun delete(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<Unit>

    @GET("block")
    suspend fun findMany(
        @Header("Authorization") token: String,
    ): Response<List<Block>>
}