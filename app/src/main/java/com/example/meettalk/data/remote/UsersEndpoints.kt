package com.example.meettalk.data.remote

import com.example.meettalk.data.local.model.body.UpdateUser
import com.example.meettalk.data.local.model.entities.Block
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersEndpoints {
    @PATCH("user")
    suspend fun update(
        @Header("Authorization") token: String,
        @Body body: UpdateUser
    ): Response<User?>

    @GET("block")
    suspend fun manyBlocks(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<List<Block>>

    @GET("user")
    suspend fun findRandom(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<User>>

    @GET("user/{userUuid}")
    suspend fun find(
        @Header("Authorization") token: String,
        @Path("userUuid") uuid: String,
    ): Response<User>

    @Multipart
    @POST("image-profile")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Header("Authorization") authHeader: String
    ): Response<ImageProfile>

    @Multipart
    @PATCH("image-profile/{uuid}")
    suspend fun updateImage(
        @Part file: MultipartBody.Part,
        @Header("Authorization") authHeader: String,
        @Path("uuid") uuid: String
    ): Response<ImageProfile>

    @GET("user/random/get")
    suspend fun random(
        @Header("Authorization") authHeader: String,
    ): Response<User>
}