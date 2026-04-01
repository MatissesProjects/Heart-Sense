package com.heart.sense.data

import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthServicesRepository @Inject constructor(
    private val healthServicesClient: HealthServicesClient
) {
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient

    suspend fun startPassiveMonitoring(serviceClass: Class<out PassiveListenerService>) {
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .setShouldUserActivityInfoBeRequested(true)
            .build()
        
        passiveMonitoringClient.setPassiveListenerServiceAsync(serviceClass, config).await()
    }

    suspend fun stopPassiveMonitoring() {
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }
}
