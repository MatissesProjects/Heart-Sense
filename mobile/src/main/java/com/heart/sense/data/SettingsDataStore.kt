package com.heart.sense.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore @Inject constructor(private val context: Context) {
    private val HIGH_HR_THRESHOLD = intPreferencesKey("high_hr_threshold")
    private val IS_SICK_MODE = booleanPreferencesKey("is_sick_mode")

    val settings: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            highHrThreshold = preferences[HIGH_HR_THRESHOLD] ?: 100,
            isSickMode = preferences[IS_SICK_MODE] ?: false
        )
    }

    suspend fun updateSettings(threshold: Int, isSick: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_HR_THRESHOLD] = threshold
            preferences[IS_SICK_MODE] = isSick
        }
    }

    suspend fun updateSickMode(isSick: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SICK_MODE] = isSick
        }
    }
}
