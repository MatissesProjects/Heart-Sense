package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentalContextDao {
    @Insert
    suspend fun insert(context: EnvironmentalContext)

    @Query("SELECT * FROM environmental_context ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<EnvironmentalContext?>

    @Query("SELECT * FROM environmental_context WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getRecent(since: String): Flow<List<EnvironmentalContext>>
}
