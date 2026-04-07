package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.heart.sense.data.Alert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: Alert)

    @Query("UPDATE alerts SET tag = :tag WHERE id = :id")
    suspend fun updateTag(id: Int, tag: String)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT 20")
    fun getRecentAlerts(): Flow<List<Alert>>

    @Query("DELETE FROM alerts WHERE timestamp < :timestamp")
    suspend fun deleteOldAlerts(timestamp: String)
}
