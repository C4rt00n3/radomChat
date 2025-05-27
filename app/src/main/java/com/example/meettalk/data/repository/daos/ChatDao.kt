package com.example.meettalk.data.repository.daos

import androidx.room.*
import com.example.meettalk.data.local.model.entities.ChatEntity
import com.example.meettalk.data.local.model.entities.ChatWithMessagesAndUsers

@Dao
interface ChatDao {

    @Transaction
    @Query("SELECT * FROM chat WHERE uuid = :chatId")
    suspend fun getChatWithMessagesAndUsers(chatId: String): ChatWithMessagesAndUsers?

    @Transaction
    @Query("SELECT * FROM chat")
    suspend fun getAllChatsWithMessagesAndUsers(): List<ChatWithMessagesAndUsers>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)
}
