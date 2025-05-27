package com.example.meettalk.data.local.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.meettalk.data.local.model.body.enums.State
import java.util.UUID

@Entity(tableName = "localizacao")
data class LocationEntity(
    @PrimaryKey val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "lat") val latitude: String? = null,
    @ColumnInfo(name = "lng") val longitude: String? = null,
    @ColumnInfo(name = "estado") val state: State? = null,
    @ColumnInfo(name = "municipio") val city: String? = null
)

