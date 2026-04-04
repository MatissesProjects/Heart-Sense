package com.heart.sense.wear.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [OvernightMeasurement::class], version = 1, exportSchema = false)
abstract class HeartSenseDatabase : RoomDatabase() {
    abstract fun overnightMeasurementDao(): OvernightMeasurementDao
}
