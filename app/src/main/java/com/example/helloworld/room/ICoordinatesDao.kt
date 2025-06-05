package com.example.helloworld.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ICoordinatesDao {
    @Insert
    suspend fun insert(coordinate: CoordinatesEntity)

    @Query("SELECT * FROM coordinates ORDER BY timestamp DESC")
    suspend fun getAll(): List<CoordinatesEntity>

    @Query("DELETE FROM coordinates WHERE timestamp = :timestamp")
    suspend fun deleteWithTimestamp(timestamp: Long)

    @Update
    suspend fun updateCoordinate(coordinate: CoordinatesEntity)
}