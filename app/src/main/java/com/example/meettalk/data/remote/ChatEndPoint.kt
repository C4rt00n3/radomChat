package com.example.meettalk.data.remote

import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.DeleteMessageBody
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID
import kotlin.uuid.Uuid

interface ChatEndPoint {
    @GET("message/{uuid}")
    suspend fun find(
        @Header("Authorization") token: String,
        @Path("uuid") uuid: String
    ): Response<Message?>

    @GET("message")
    suspend fun findAll(
        @Header("Authorization") token: String
    ): Response<List<Chat>>

    @HTTP(method = "DELETE", path = "message", hasBody = true)
    suspend fun delete(
        @Header("Authorization") token: String,
        @Body body: DeleteMessageBody, // Note a vírgula adicionada aqui
        @Query("safe") safe: Boolean = false // Ajuste aqui para definir o nome e o tipo do parâmetro de query
    ): Response<Unit>

    @POST("message")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body body: CreateMessage
    ): Response<Message>

    @PATCH("message/{uuid}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("uuid") uuid: String,
        @Body body: UpdateMessage
    ): Response<Message>

    @GET("message/markRead/{chat_uuid}")
    suspend fun markRead(
        @Header("Authorization") token: String,
        @Path("chat_uuid") uuid: String
    ): Response<Unit>

    @GET("verifyToken")
    suspend fun verifyToken(
        @Header("Authorization") token: String,
    ): Response<Unit>

    @GET("message/listMessageRemoved")
    suspend fun listMessageRemoved(
        @Header("Authorization") token: String,
    ): Response<List<String>>

    @GET("message/list/updated/")
    suspend fun listMessagesUpdated(
        @Header("Authorization") token: String,
    ): Response<List<Message>>

    @GET("chat/{chatUuid}")
    suspend fun findOneChat(
        @Path("chatUuid") uuid: String,
        @Header("Authorization") token: String,
    ): Response<Chat?>

    @PATCH("chat/{uuid}")
    suspend fun markFav(
        @Path("uuid") uuid: String,
        @Header("Authorization") token: String,
    ): Response<Chat?>
}
