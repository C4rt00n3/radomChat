package com.example.meettalk.data.remote

import android.util.Log
import com.example.meettalk.data.local.model.entities.ImageProfile
import com.example.meettalk.data.local.model.entities.User
import io.realm.kotlin.Realm
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRequests(private val url: String, private val realm: Realm) {
    private val retrofit =
        Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()

    private val apiServiceChat = retrofit.create(UsersEndpoints::class.java)
    suspend fun uploadImage(
        file: MultipartBody.Part,
        token: String,
        slot: Int = 0
    ): ImageProfile? {
        return try {
            val response = apiServiceChat.uploadImage(file, slot, token)
            if(response.isSuccessful)
                response.body()
            else {
                println(response.message())
                println(response.errorBody())
                Log.e("Error ao atualizar imagem", response.errorBody().toString())
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun random(
        token: String
    ): User? {
        return try {
            val response = apiServiceChat.random(token)
            if(response.isSuccessful)
                response.body()
            else {
                println(response.message())
                println(response.errorBody())
                Log.e("Error ao buscar usuario", response.errorBody().toString())
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUser(uuid: String, token: String): User? {
        return try {
            if(token.isBlank())
                return null

            val response =  apiServiceChat.find(token, uuid)
            if(response.isSuccessful)
                response.body()
            else {
                println(response.message())
                println(response.errorBody())
                Log.e("Error ao buscar Ususario", response.errorBody().toString())
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}