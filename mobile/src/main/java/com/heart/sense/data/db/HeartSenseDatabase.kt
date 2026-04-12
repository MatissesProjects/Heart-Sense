package com.heart.sense.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heart.sense.data.*

@Database(entities = [
    OvernightMeasurement::class, 
    Alert::class, 
    Intervention::class, 
    Session::class, 
    Medication::class, 
    MedicationIntake::class, 
    BloodGlucose::class,
    LocationTag::class,
    EnvironmentalContext::class,
    FhirExportLog::class,
    CbtJournalEntry::class,
    BleSensorData::class
], version = 12, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HeartSenseDatabase : RoomDatabase() {
    abstract fun overnightMeasurementDao(): OvernightMeasurementDao
    abstract fun alertDao(): AlertDao
    abstract fun interventionDao(): InterventionDao
    abstract fun sessionDao(): SessionDao
    abstract fun medicationDao(): MedicationDao
    abstract fun bloodGlucoseDao(): BloodGlucoseDao
    abstract fun locationDao(): LocationDao
    abstract fun environmentalContextDao(): EnvironmentalContextDao
    abstract fun fhirExportLogDao(): FhirExportLogDao
    abstract fun cbtJournalDao(): CbtJournalDao
    abstract fun bleSensorDao(): BleSensorDao
}
