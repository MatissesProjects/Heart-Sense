package com.heart.sense.data

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.heart.sense.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class SettingsRepository @Inject constructor(
    private val dataClient: DataClient,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    suspend fun updateSettings(settings: Settings) {
        settingsDataStore.updateSettings(settings.highHrThreshold, settings.isSickMode)
        debouncedSync(settings)
    }

    suspend fun updateSickMode(isSick: Boolean) {
        settingsDataStore.updateSickMode(isSick)
        val currentSettings = settingsDataStore.settings.first()
        debouncedSync(currentSettings)
    }

    private fun debouncedSync(settings: Settings) {
        syncJob?.cancel()
        syncJob = scope.launch {
            delay(500) // Debounce for 500ms
            sync(settings)
        }
    }

    private suspend fun sync(settings: Settings) {
        val request = PutDataMapRequest.create(Constants.PATH_SETTINGS).apply {
            dataMap.putInt(Constants.KEY_HIGH_HR_THRESHOLD, settings.highHrThreshold)
            dataMap.putBoolean(Constants.KEY_IS_SICK_MODE, settings.isSickMode)
        }.asPutDataRequest().setUrgent()
        
        try {
            dataClient.putDataItem(request).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
}
