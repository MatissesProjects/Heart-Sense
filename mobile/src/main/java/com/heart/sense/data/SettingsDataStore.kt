package com.heart.sense.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.heart.sense.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(private val context: Context) {
    private val HIGH_HR_THRESHOLD = intPreferencesKey(Constants.KEY_HIGH_HR_THRESHOLD)
    private val IS_SICK_MODE = booleanPreferencesKey(Constants.KEY_IS_SICK_MODE)
    private val LAST_UPDATED = longPreferencesKey(Constants.KEY_LAST_UPDATED)
    private val SNOOZE_UNTIL = longPreferencesKey(Constants.KEY_SNOOZE_UNTIL)
    private val CALIBRATION_STATUS = stringPreferencesKey(Constants.KEY_CALIBRATION_STATUS)
    private val RESTING_HR = intPreferencesKey(Constants.KEY_RESTING_HR)
    private val RESPIRATORY_RATE = floatPreferencesKey(Constants.KEY_RESPIRATORY_RATE)
    private val CALIBRATION_START_TIME = longPreferencesKey(Constants.KEY_CALIBRATION_START_TIME)

    val settings: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            highHrThreshold = preferences[HIGH_HR_THRESHOLD] ?: 100,
            isSickMode = preferences[IS_SICK_MODE] ?: false,
            lastUpdated = preferences[LAST_UPDATED] ?: 0L,
            snoozeUntil = preferences[SNOOZE_UNTIL] ?: 0L,
            calibrationStatus = preferences[CALIBRATION_STATUS] ?: "NOT_STARTED",
            restingHr = preferences[RESTING_HR] ?: 0,
            respiratoryRate = preferences[RESPIRATORY_RATE] ?: 0f,
            calibrationStartTime = preferences[CALIBRATION_START_TIME] ?: 0L
        )
    }

    suspend fun updateSettings(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_HR_THRESHOLD] = settings.highHrThreshold
            preferences[IS_SICK_MODE] = settings.isSickMode
            preferences[LAST_UPDATED] = settings.lastUpdated
            preferences[SNOOZE_UNTIL] = settings.snoozeUntil
            preferences[CALIBRATION_STATUS] = settings.calibrationStatus
            preferences[RESTING_HR] = settings.restingHr
            preferences[RESPIRATORY_RATE] = settings.respiratoryRate
            preferences[CALIBRATION_START_TIME] = settings.calibrationStartTime
        }
    }

    suspend fun updateSettings(highHrThreshold: Int, isSick: Boolean, timestamp: Long, snoozeUntil: Long = 0L) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_HR_THRESHOLD] = highHrThreshold
            preferences[IS_SICK_MODE] = isSick
            preferences[LAST_UPDATED] = timestamp
            preferences[SNOOZE_UNTIL] = snoozeUntil
        }
    }

    suspend fun updateSickMode(isSick: Boolean, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[IS_SICK_MODE] = isSick
            preferences[LAST_UPDATED] = timestamp
        }
    }

    suspend fun setSnooze(until: Long) {
        context.dataStore.edit { preferences ->
            preferences[SNOOZE_UNTIL] = until
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }
}
