package com.example.meettalk.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

open class SocketManager(private val baseUrl: String, context: Context) {
    private var socket: Socket? = null
    private var isConnected = false

    init {
        val opts = IO.Options().apply {
            transports = arrayOf("websocket") // Force WebSocket usage
            reconnection = true // Reconnect if the connection is lost
        }

        socket = IO.socket(baseUrl, opts)
    }

    fun on(event: String, callback: (Array<Any>) -> Unit) {
        socket?.on(event) { args ->
            callback(args)
        }
    }

    open fun connect() {
        socket?.connect()
    }

    open fun disconnect() {
        socket?.disconnect()
    }

    fun isConnected(): Boolean {
        return isConnected
    }
}

