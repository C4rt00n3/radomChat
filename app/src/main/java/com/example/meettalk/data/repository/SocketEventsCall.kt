package com.example.meettalk.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.meettalk.presentation.viewmodel.ChatViewModel
import com.example.meettalk.utils.TaskManager
import io.socket.client.Socket

@RequiresApi(Build.VERSION_CODES.O)
fun connectSocket(socketManager: SocketManager,chatViewModel: ChatViewModel, callIsConnected: (Boolean) -> Unit) {
    val taskManager = TaskManager()

    socketManager.connect()

    chatViewModel.apply {
        socketManager.apply {
            on(Socket.EVENT_CONNECT) {
                callIsConnected(true)
                println("Connected to server")
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                callIsConnected(false)
                println("Disconnected from server")
                if (args.isNotEmpty()) {
                    println("Reason for disconnect: ${args[0]}")
                    if (args[0] is Throwable) {
                        (args[0] as Throwable).printStackTrace()
                    }
                }
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                println("🔁 Reconnect error:")
                args.forEach { println("  ➤ $it") }
                if (args.isNotEmpty() && args[0] is Throwable) {
                    (args[0] as Throwable).printStackTrace()
                }
            }

            on("messageReady") { taskManager.addTask { messageReady(it) } }

            on("contactProfileUpdated") { args -> taskManager.addTask { contactProfileUpdated(args) } }

            on("contactImageProfileUpdated") { args ->
                taskManager.addTask {
                    contactImageProfileUpdated(
                        args
                    )
                }
            }

            on("message") { args -> taskManager.addTask { onMessageReceived(args) } }

            on("idsChats") {
                taskManager.addTask { newChats(it) }
            }

            on("listMessageRemoved") { taskManager.addTask { listMessageRemoved(it) } }

            on("updateMessage") { taskManager.addTask { updateMessage(it) } }

            on("removeMessages") { taskManager.addTask { removeMessages(it) } }
        }
    }
}