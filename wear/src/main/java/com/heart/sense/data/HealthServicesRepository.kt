package com.heart.sense.data

import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthServicesRepository @Inject constructor(
    private val healthServicesClient: HealthServicesClient
) {
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    private val measureClient = healthServicesClient.measureClient

    fun getMeasureData(dataType: DeltaDataType<*, *>) : Flow<DataPointContainer> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onDataReceived(data: DataPointContainer) {
                trySend(data)
            }

            override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: androidx.health.services.client.data.Availability) {
                // Handle availability changes if needed
            }
        }

        measureClient.registerMeasureCallback(dataType, callback)

        awaitClose {
            measureClient.unregisterMeasureCallback(dataType, callback)
        }
    }

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
