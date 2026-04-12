package com.heart.sense.wear.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [OvernightMeasurement::class, Medication::class], version = 3, exportSchema = false)
abstract class HeartSenseDatabase : RoomDatabase() {
    abstract fun overnightMeasurementDao(): OvernightMeasurementDao
    abstract fun medicationDao(): MedicationDao
}
