package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.heart.sense.data.Alert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert
    suspend fun insert(alert: Alert): Long

    @Query("SELECT * FROM alerts WHERE id = :id LIMIT 1")
    suspend fun getAlertById(id: Int): Alert?

    @Query("UPDATE alerts SET tag = :tag WHERE id = :id")
    suspend fun updateTag(id: Int, tag: String)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT 20")
    fun getRecentAlerts(): Flow<List<Alert>>

    @Query("SELECT * FROM alerts WHERE visitId = :visitId ORDER BY timestamp ASC")
    suspend fun getAlertsByVisitId(visitId: String): List<Alert>

    @Query("DELETE FROM alerts WHERE timestamp < :timestamp")
    suspend fun deleteOldAlerts(timestamp: String)
}
