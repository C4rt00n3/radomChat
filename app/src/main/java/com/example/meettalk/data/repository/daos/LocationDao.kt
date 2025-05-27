package com.example.meettalk.data.repository.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meettalk.data.local.model.entities.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM localizacao WHERE uuid = :id")
    suspend fun getLocationById(id: String): LocationEntity?

    @Query("SELECT * FROM localizacao")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("DELETE FROM localizacao WHERE uuid = :id")
    suspend fun deleteLocationById(id: String)
}