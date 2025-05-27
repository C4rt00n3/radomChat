package com.example.meettalk.data.remote

import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.DeleteMessageBody
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.data.local.model.entities.MessageEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface ChatEndPoint {
    @GET("message/{uuid}")
    suspend fun find(
        @Header("Authorization") token: String,
        @Path("uuid") uuid: UUID
    ): Response<MessageEntity?>

    @GET("message")
    suspend fun findAll(
        @Header("Authorization") token: String
    ): Response<List<ChatEntity>>

    @HTTP(method = "DELETE", path = "message", hasBody = true)
    suspend fun delete(
        @Header("Authorization") token: String,
        @Body body: DeleteMessageBody
    ): Response<Unit>

    @POST("message")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body body: CreateMessage
    ): Response<MessageEntity>

    @PATCH("message/{uuid}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("uuid") uuid: String,
        @Body body: UpdateMessage
    ): Response<MessageEntity>

    @GET("message/markRead/{chat_uuid}")
    suspend fun markRead(
        @Header("Authorization") token: String,
        @Path("chat_uuid") uuid: String
    ): Response<Unit>

    @GET("verifyToken")
    suspend fun verifyToken(
        @Header("Authorization") token: String,
    ): Response<Unit>

}
