package com.heart.sense.wear.di

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.room.Room
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.heart.sense.wear.data.SettingsDataStore
import com.heart.sense.wear.data.db.HeartSenseDatabase
import com.heart.sense.wear.data.db.OvernightMeasurementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideHealthServicesClient(@ApplicationContext context: Context): HealthServicesClient {
        return HealthServices.getClient(context)
    }

    @Provides
    @Singleton
    fun provideMessageClient(@ApplicationContext context: Context): MessageClient {
        return Wearable.getMessageClient(context)
    }

    @Provides
    @Singleton
    fun provideDataClient(@ApplicationContext context: Context): DataClient {
        return Wearable.getDataClient(context)
    }

    @Provides
    @Singleton
    fun provideCapabilityClient(@ApplicationContext context: Context): CapabilityClient {
        return Wearable.getCapabilityClient(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HeartSenseDatabase {
        return Room.databaseBuilder(
            context,
            HeartSenseDatabase::class.java,
            "heart_sense_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideOvernightMeasurementDao(database: HeartSenseDatabase): OvernightMeasurementDao {
        return database.overnightMeasurementDao()
    }

    @Provides
    @Singleton
    fun provideGamificationRepository(settingsDataStore: SettingsDataStore): com.heart.sense.wear.data.GamificationRepository {
        return com.heart.sense.wear.data.GamificationRepository(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideMultiModalDataAggregator(): com.heart.sense.wear.ai.MultiModalDataAggregator {
        return com.heart.sense.wear.ai.MultiModalDataAggregator()
    }

    @Provides
    @Singleton
    fun provideStressPredictor(@ApplicationContext context: Context): com.heart.sense.wear.ai.StressPredictor {
        return com.heart.sense.wear.ai.StressPredictor(context)
    }
}
