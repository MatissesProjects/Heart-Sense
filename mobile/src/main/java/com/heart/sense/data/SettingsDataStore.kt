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
    private val EMERGENCY_CONTACT_NAME = stringPreferencesKey(Constants.KEY_EMERGENCY_CONTACT_NAME)
    private val EMERGENCY_CONTACT_PHONE = stringPreferencesKey(Constants.KEY_EMERGENCY_CONTACT_PHONE)
    private val EMERGENCY_COUNTDOWN = intPreferencesKey(Constants.KEY_EMERGENCY_COUNTDOWN)
    private val EMERGENCY_ENABLED = booleanPreferencesKey(Constants.KEY_EMERGENCY_ENABLED)
    private val DETECT_PACING = booleanPreferencesKey(Constants.KEY_DETECT_PACING)
    private val DETECT_AGITATION = booleanPreferencesKey(Constants.KEY_DETECT_AGITATION)
    private val CURRENT_STREAK = intPreferencesKey(Constants.KEY_CURRENT_STREAK)
    private val BEST_STREAK = intPreferencesKey(Constants.KEY_BEST_STREAK)
    private val CALM_POINTS = intPreferencesKey(Constants.KEY_CALM_POINTS)

    val settings: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            highHrThreshold = preferences[HIGH_HR_THRESHOLD] ?: 100,
            isSickMode = preferences[IS_SICK_MODE] ?: false,
            lastUpdated = preferences[LAST_UPDATED] ?: 0L,
            snoozeUntil = preferences[SNOOZE_UNTIL] ?: 0L,
            calibrationStatus = preferences[CALIBRATION_STATUS] ?: "NOT_STARTED",
            restingHr = preferences[RESTING_HR] ?: 0,
            respiratoryRate = preferences[RESPIRATORY_RATE] ?: 0f,
            calibrationStartTime = preferences[CALIBRATION_START_TIME] ?: 0L,
            emergencyContactName = preferences[EMERGENCY_CONTACT_NAME] ?: "",
            emergencyContactPhone = preferences[EMERGENCY_CONTACT_PHONE] ?: "",
            emergencyCountdownSeconds = preferences[EMERGENCY_COUNTDOWN] ?: 30,
            isEmergencyEnabled = preferences[EMERGENCY_ENABLED] ?: false,
            detectPacing = preferences[DETECT_PACING] ?: false,
            detectAgitation = preferences[DETECT_AGITATION] ?: false,
            currentStreakMinutes = preferences[CURRENT_STREAK] ?: 0,
            bestStreakMinutes = preferences[BEST_STREAK] ?: 0,
            calmPoints = preferences[CALM_POINTS] ?: 0
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
            preferences[EMERGENCY_CONTACT_NAME] = settings.emergencyContactName
            preferences[EMERGENCY_CONTACT_PHONE] = settings.emergencyContactPhone
            preferences[EMERGENCY_COUNTDOWN] = settings.emergencyCountdownSeconds
            preferences[EMERGENCY_ENABLED] = settings.isEmergencyEnabled
            preferences[DETECT_PACING] = settings.detectPacing
            preferences[DETECT_AGITATION] = settings.detectAgitation
            preferences[CURRENT_STREAK] = settings.currentStreakMinutes
            preferences[BEST_STREAK] = settings.bestStreakMinutes
            preferences[CALM_POINTS] = settings.calmPoints
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

    suspend fun startCalibration() {
        context.dataStore.edit { preferences ->
            preferences[CALIBRATION_STATUS] = "CALIBRATING"
            preferences[CALIBRATION_START_TIME] = System.currentTimeMillis()
            preferences[LAST_UPDATED] = System.currentTimeMillis()
        }
    }
}
