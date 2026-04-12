package com.heart.sense.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.wearable.*
import com.heart.sense.data.SettingsDataStore
import com.heart.sense.data.db.HeartSenseDatabase
import com.heart.sense.data.db.OvernightMeasurementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearableModule {
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideDataClient(@ApplicationContext context: Context): DataClient {
        return Wearable.getDataClient(context)
    }

    @Provides
    @Singleton
    fun provideMessageClient(@ApplicationContext context: Context): MessageClient {
        return Wearable.getMessageClient(context)
    }

    @Provides
    @Singleton
    fun provideCapabilityClient(@ApplicationContext context: Context): CapabilityClient {
        return Wearable.getCapabilityClient(context)
    }

    @Provides
    @Singleton
    fun provideNodeClient(@ApplicationContext context: Context): NodeClient {
        return Wearable.getNodeClient(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HeartSenseDatabase {
        return Room.databaseBuilder(
            context,
            HeartSenseDatabase::class.java,
            "heart_sense_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideOvernightMeasurementDao(database: HeartSenseDatabase): OvernightMeasurementDao {
        return database.overnightMeasurementDao()
    }

    @Provides
    @Singleton
    fun provideAlertDao(database: HeartSenseDatabase): com.heart.sense.data.db.AlertDao {
        return database.alertDao()
    }

    @Provides
    @Singleton
    fun provideInterventionDao(database: HeartSenseDatabase): com.heart.sense.data.db.InterventionDao {
        return database.interventionDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: HeartSenseDatabase): com.heart.sense.data.db.SessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun provideMedicationDao(database: HeartSenseDatabase): com.heart.sense.data.db.MedicationDao {
        return database.medicationDao()
    }

    @Provides
    @Singleton
    fun provideBloodGlucoseDao(database: HeartSenseDatabase): com.heart.sense.data.db.BloodGlucoseDao {
        return database.bloodGlucoseDao()
    }

    @Provides
    @Singleton
    fun provideLocationDao(database: HeartSenseDatabase): com.heart.sense.data.LocationDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideEnvironmentalContextDao(database: HeartSenseDatabase): com.heart.sense.data.db.EnvironmentalContextDao {
        return database.environmentalContextDao()
    }

    @Provides
    @Singleton
    fun provideFhirExportLogDao(database: HeartSenseDatabase): com.heart.sense.data.db.FhirExportLogDao {
        return database.fhirExportLogDao()
    }

    @Provides
    @Singleton
    fun provideCbtJournalDao(database: HeartSenseDatabase): com.heart.sense.data.db.CbtJournalDao {
        return database.cbtJournalDao()
    }

    @Provides
    @Singleton
    fun provideBleSensorDao(database: HeartSenseDatabase): com.heart.sense.data.db.BleSensorDao {
        return database.bleSensorDao()
    }
}
