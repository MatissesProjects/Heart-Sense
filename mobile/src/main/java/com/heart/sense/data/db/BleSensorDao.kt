package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BleSensorDao {
    @Insert
    suspend fun insert(data: BleSensorData)

    @Query("SELECT * FROM ble_sensor_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<BleSensorData?>

    @Query("SELECT * FROM ble_sensor_data WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getRecent(since: String): Flow<List<BleSensorData>>

    @Query("DELETE FROM ble_sensor_data WHERE timestamp < :timestamp")
    suspend fun deleteOldData(timestamp: String)
}
