package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OvernightMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: OvernightMeasurement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<OvernightMeasurement>)

    @Query("SELECT * FROM overnight_measurements WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getMeasurementsInRange(startTime: Long, endTime: Long): List<OvernightMeasurement>

    @Query("DELETE FROM overnight_measurements WHERE timestamp < :timestamp")
    suspend fun deleteOldMeasurements(timestamp: Long)
}
