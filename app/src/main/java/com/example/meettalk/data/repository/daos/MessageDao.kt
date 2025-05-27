package com.example.meettalk.data.repository.daos

import androidx.room.*
import com.example.meettalk.data.local.model.entities.MessageEntity

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM message WHERE uuid = :id")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("SELECT * FROM message WHERE chat_uuid = :chatId ORDER BY createdAt ASC")
    suspend fun getMessagesByChat(chatId: String): List<MessageEntity>

    @Query("DELETE FROM message WHERE uuid = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM message")
    suspend fun deleteAllMessages()
}