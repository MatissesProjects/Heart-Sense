package com.heart.sense.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heart.sense.data.Alert

@Database(entities = [OvernightMeasurement::class, Alert::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HeartSenseDatabase : RoomDatabase() {
    abstract fun overnightMeasurementDao(): OvernightMeasurementDao
    abstract fun alertDao(): AlertDao
}
