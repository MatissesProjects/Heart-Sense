package com.heart.sense.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AdvancedSensorData(
    val skinTemp: Float? = null,
    val eda: Float? = null,
    val manualTemp: Float? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Singleton
class AdvancedSensorRepository @Inject constructor() {
    private val _sensorData = MutableStateFlow(AdvancedSensorData())
    val sensorData = _sensorData.asStateFlow()

    fun updateSkinTemp(temp: Float) {
        _sensorData.value = _sensorData.value.copy(
            skinTemp = temp,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun updateEda(eda: Float) {
        _sensorData.value = _sensorData.value.copy(
            eda = eda,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun setManualTemp(temp: Float) {
        _sensorData.value = _sensorData.value.copy(
            manualTemp = temp,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
