package com.heart.sense.data

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataClient: DataClient
) {
    suspend fun updateSettings(settings: Settings) {
        val request = PutDataMapRequest.create("/settings").apply {
            dataMap.putInt("highHrThreshold", settings.highHrThreshold)
            dataMap.putBoolean("isSickMode", settings.isSickMode)
        }.asPutDataRequest().setUrgent()
        
        try {
            dataClient.putData(request).await()
        } catch (e: Exception) {
            // Handle error (e.g., log it)
        }
    }
}
