package com.example.meettalk.data.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "autenticacao"
)
data class AuthEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String
)