package com.heart.sense.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(private val context: Context) {
    private val HIGH_HR_THRESHOLD = intPreferencesKey("high_hr_threshold")
    private val IS_SICK_MODE = booleanPreferencesKey("is_sick_mode")
    private val LAST_UPDATED = longPreferencesKey("last_updated")

    val settings: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            highHrThreshold = preferences[HIGH_HR_THRESHOLD] ?: 100,
            isSickMode = preferences[IS_SICK_MODE] ?: false,
            lastUpdated = preferences[LAST_UPDATED] ?: 0L
        )
    }

    suspend fun updateSettings(highHrThreshold: Int, isSick: Boolean, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_HR_THRESHOLD] = highHrThreshold
            preferences[IS_SICK_MODE] = isSick
            preferences[LAST_UPDATED] = timestamp
        }
    }

    suspend fun updateSickMode(isSick: Boolean, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[IS_SICK_MODE] = isSick
            preferences[LAST_UPDATED] = timestamp
        }
    }
}
