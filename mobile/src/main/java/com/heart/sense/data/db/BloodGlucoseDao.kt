package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BloodGlucoseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BloodGlucose)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BloodGlucose>)

    @Query("SELECT * FROM blood_glucose_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getRecordsInRange(startTime: Long, endTime: Long): List<BloodGlucose>

    @Query("SELECT * FROM blood_glucose_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRecord(): BloodGlucose?
}
