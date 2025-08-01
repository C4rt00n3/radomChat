package com.example.meettalk.data.remote

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.meettalk.data.local.model.body.BodyUpdatesUsers
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.DeleteMessageBody
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.data.local.model.entities.User
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import com.google.gson.Gson
import io.realm.kotlin.Realm
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatRequests(private val url: String, private val realm: Realm) {
    private val format = FormatRealm()
    private val formatR = FormatClass()

    private val retrofit =
        Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()

    private val apiServiceChat = retrofit.create(ChatEndPoint::class.java)

    suspend fun manyRequest(token: String): List<Chat> {
        val response: Response<List<Chat>> = apiServiceChat.findAll(token)
        if (response.isSuccessful) {
            return response.body() ?: listOf()
        }
        return listOf()
    }

    suspend fun deleteMany(
        token: String,
        list: List<String>,
        safe: Boolean,
        onError: (Exception) -> Unit = {},
        onSuccess: (uuids: List<String>) -> Unit = {},
    ): Unit {
        try {
            val body = DeleteMessageBody(list)

            val response = apiServiceChat.delete(token, body, safe)

            if (response.isSuccessful) {
                onSuccess(list)
            } else {
                Log.e("Chat", "Erro ao deletar mensagens: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("Chat", "Exceção ao deletar mensagens: ${e.message}", e)
            onError(e)
        }
    }

    suspend fun find(uuid: String, token: String): Message? {
        try {
            return apiServiceChat.find(token, uuid).body()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun create(
        token: String,
        data: CreateMessage,
        file: MultipartBody.Part? = null,
        finally: () -> Unit = {},
        error: (Exception) -> Unit = {},
        onSuccess: (Message) -> Unit = {}
    ): Message? {
        return try {
            val response = apiServiceChat.create(
                token = token,
                text = data.text.toRequestBody("text/plain".toMediaTypeOrNull()),
                receiverId = data.receiverId.toRequestBody("text/plain".toMediaTypeOrNull()),
                type = data.type.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                replyToId = data.replyToId?.toRequestBody("text/plain".toMediaTypeOrNull()),
                file = file
            )

            if (response.isSuccessful) {
                val newMessage = response.body()
                if (newMessage != null)
                    onSuccess(newMessage)
                newMessage
            } else {
                Log.d("Error", "Erro na resposta: ${response.code()}")
                throw RuntimeException(
                    "Erro na resposta: ${response.code()} - ${
                        response.errorBody()?.string()
                    }"
                )
            }
        } catch (e: Exception) {
            error(e)
            Log.d("Error", e.message.orEmpty())
            e.printStackTrace()
            null
        } finally {
            finally()
        }
    }

    suspend fun update(
        token: String,
        id: String,
        data: UpdateMessage,
        onFinally: () -> Unit
    ): Message? {
        return try {
            val response = apiServiceChat.update(token, id, data)

            if (response.isSuccessful)
                response.body()
            else {
                Log.d("Error", "Erro na resposta: ${response.code()}")
                null
            }
        } catch (error: Exception) {
            Log.d("Error", error.message.toString())
            null
        } finally {
            onFinally()
        }
    }

    suspend fun markRead(
        token: String,
        uuid: String,
        onError: ((Throwable) -> Unit)? = null,
        onSuccess: () -> Unit
    ) {
        try {
            val response = apiServiceChat.markRead(token = token, uuid = uuid)
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError?.invoke(HttpException(response))
            }
        } catch (error: Exception) {
            Log.d("Error", error.message.orEmpty())
            error.printStackTrace()
            onError?.invoke(error)
        }
    }

    suspend fun listMessageRemoved(token: String, onSuccess: () -> Unit): List<String> {
        return try {
            val response = apiServiceChat.listMessageRemoved(token = token)

            if (response.isSuccessful)
                response.body() ?: emptyList()
            else {
                emptyList()
            }
        } catch (error: Exception) {
            Log.d("Error", error.message.orEmpty())
            error.printStackTrace()
            emptyList()
        } finally {
            onSuccess()
        }
    }

    suspend fun listMessagesUpdated(token: String): List<Message> {
        return try {
            val response = apiServiceChat.listMessagesUpdated(token)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.d("API Error", "Erro ${response.code()}: ${response.message()}")
                emptyList()
            }
        } catch (error: Exception) {
            Log.d("Exception", error.message.orEmpty())
            error.printStackTrace()
            emptyList()
        }
    }

    suspend fun findOneChat(chatUuid: String, token: String): Chat? {
        return try {
            val response = apiServiceChat.findOneChat(uuid = chatUuid, token = token)

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.d("API Error", "Erro ${response.code()}: ${response.message()}")
                null
            }
        } catch (error: Exception) {
            Log.d("Exception", error.message.orEmpty())
            error.printStackTrace()
            null
        }
    }

    suspend fun markFav(uuid: String, token: String): Chat? {
        return try {
            val response = apiServiceChat.markFav(uuid = uuid, token = token)

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.d("API Error", "Erro ${response.code()}: ${response.message()}")
                null
            }
        } catch (error: Exception) {
            Log.d("Exception", error.message.orEmpty())
            error.printStackTrace()
            null
        }
    }
}