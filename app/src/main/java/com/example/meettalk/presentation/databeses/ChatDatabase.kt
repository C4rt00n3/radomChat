package com.example.meettalk.presentation.databeses

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.meettalk.data.local.model.entities.AuthEntity
import com.example.meettalk.data.local.model.entities.BlockEntity
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.data.local.model.entities.ChatParticipantEntity
import com.example.meettalk.data.local.model.entities.ChatUserCrossRef
import com.example.meettalk.data.local.model.entities.ChatWithMessagesAndUsers
import com.example.meettalk.data.local.model.entities.ImageMessage
import com.example.meettalk.data.local.model.entities.ImageProfileEntity
import com.example.meettalk.data.local.model.entities.LocationEntity
import com.example.meettalk.data.local.model.entities.MessageEntity
import com.example.meettalk.data.local.model.entities.UserEntity
import com.example.meettalk.data.local.model.entities.UserWithProfileImages
import com.example.meettalk.data.repository.daos.ChatDao
import com.example.meettalk.data.repository.daos.LocationDao
import com.example.meettalk.data.repository.daos.MessageDao
import com.example.meettalk.data.repository.daos.UserDao

/**
 * Representa o banco de dados principal da aplicação usando Room.
 * Inclui entidades relacionadas a chats, mensagens, participantes e usuários.
 */
@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        ChatParticipantEntity::class,
        UserEntity::class,
        ImageProfileEntity::class,
        ImageMessage::class,
        BlockEntity::class,
        LocationEntity::class,
        ChatUserCrossRef::class
    ],
    version = 7,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract  fun messageDao(): MessageDao
}

