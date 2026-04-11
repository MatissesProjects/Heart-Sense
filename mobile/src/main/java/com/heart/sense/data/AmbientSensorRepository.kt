package com.heart.sense.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmbientSensorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    /**
     * Provides a flow of ambient temperature updates in Celsius.
     */
    fun getAmbientTemp(): Flow<Float> = callbackFlow {
        if (tempSensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    trySend(event.values[0])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, tempSensor, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
