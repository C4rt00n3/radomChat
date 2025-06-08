package com.example.meettalk.data.remote

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.meettalk.data.local.model.body.CreateMessage
import com.example.meettalk.data.local.model.body.DeleteMessageBody
import com.example.meettalk.data.local.model.body.UpdateMessage
import com.example.meettalk.data.local.model.entities.Chat
import com.example.meettalk.data.local.model.entities.Message
import com.example.meettalk.utils.FormatClass
import com.example.meettalk.utils.FormatRealm
import io.realm.kotlin.Realm
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

    open suspend fun manyRequest(token: String): List<Chat> {
        val response: Response<List<Chat>> = apiServiceChat.findAll(token)
        if (response.isSuccessful) {
            return response.body() ?: listOf()
        }
        return listOf()
    }

    open suspend fun deleteMany(
        token: String,
        list: List<Message>,
        change: (uuids: List<String>) -> Unit
    ): Unit {
        try {
            val uuids = list.map { it.uuid }
            val body = DeleteMessageBody(uuids)

            val response = apiServiceChat.delete(token, body)

            if (response.isSuccessful) {
                change(uuids)
            } else {
                Log.e("Chat", "Erro ao deletar mensagens: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("Chat", "Exceção ao deletar mensagens: ${e.message}", e)
        }
    }

    open suspend fun find(uuid: String, token: String): Message? {
        try {
            return apiServiceChat.find(token, uuid).body()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    open suspend fun create(
        token: String,
        data: CreateMessage,
        error: (Exception) -> Unit = {},
        onSuccess: (Message) -> Unit = {}
    ): Message? {
        return try {
            val response = apiServiceChat.create(token, data)
            if (response.isSuccessful) {
                val newMessage = response.body()
                newMessage?.let {
                    onSuccess(it)
                }
                newMessage
            } else {
                Log.d("Error", "Erro na resposta: ${response.code()}")
                println(response.errorBody()?.string())
                null
            }
        } catch (error: Exception) {
            error(error)
            Log.d("Error", error.message.orEmpty())
            error.printStackTrace()
            null
        }
    }

    open suspend fun update(token: String, id: String, data: UpdateMessage, chat: Chat): Chat? {
        return try {
            val response = apiServiceChat.update(token, id, data)

            if (response.isSuccessful) {
                val novaMessage = response.body()
                novaMessage?.let { message ->
                    return chat.copy(messages = chat.messages.map { msg ->
                        if (msg.uuid == message.uuid) message else msg
                    })
                }
            } else {
                Log.d("Error", "Erro na resposta: ${response.code()}")
                println(response.errorBody()?.string())
            }

            null
        } catch (error: Exception) {
            println(error)
            Log.d("Error", error.message.toString())
            null
        }
    }

    open suspend fun markRead(
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
}