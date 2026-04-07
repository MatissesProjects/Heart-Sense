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
}
