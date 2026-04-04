package com.heart.sense.wear.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val HIGH_HR_THRESHOLD = intPreferencesKey("high_hr_threshold")
    private val IS_SICK_MODE = booleanPreferencesKey("is_sick_mode")
    private val IS_MONITORING_ACTIVE = booleanPreferencesKey("is_monitoring_active")

    val settings: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            highHrThreshold = preferences[HIGH_HR_THRESHOLD] ?: 100,
            isSickMode = preferences[IS_SICK_MODE] ?: false
        )
    }

    val isMonitoringActive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_MONITORING_ACTIVE] ?: false
    }

    suspend fun updateSettings(highHrThreshold: Int, isSickMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_HR_THRESHOLD] = highHrThreshold
            preferences[IS_SICK_MODE] = isSickMode
        }
    }

    suspend fun setMonitoringActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_MONITORING_ACTIVE] = active
        }
    }
}
