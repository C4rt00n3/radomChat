package com.example.meettalk.data.repository.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.meettalk.data.local.model.entities.ChatUserCrossRef
import com.example.meettalk.data.local.model.entities.ImageProfileEntity
import com.example.meettalk.data.local.model.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users:List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM usuario WHERE uuid = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM usuario")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatUserCrossRefs(crossRefs: List<ChatUserCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileImages(images: List<ImageProfileEntity>)
}

