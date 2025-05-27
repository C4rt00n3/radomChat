package com.example.meettalk.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.meettalk.MainActivity
import com.example.meettalk.R

class WebSocketService : Service() {

    private lateinit var socketManager: SocketManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel() // Criar o canal de notificação
        startForeground(1, createNotification()) // Iniciar o serviço em primeiro plano

        socketManager =
            SocketManager(getString(R.string.baseUrl), this) // Usar 'this' para o contexto
        socketManager.connect()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "websocket_service")
            .setContentTitle("WebSocket Service Running")
            .setContentText("Listening for messages...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "websocket_service",
                "WebSocket Service Channel",
                NotificationManager.IMPORTANCE_HIGH // Importância alta para garantir que as notificações apareçam
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(message: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "websocket_service")
            .setContentTitle("Nova mensagem")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Garantir que a notificação seja alta
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }
}
