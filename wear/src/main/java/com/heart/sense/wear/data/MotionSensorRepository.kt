package com.heart.sense.wear.data

import android.content.Context
import android.hardware.Sensor
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

data class MotionData(
    val acceleration: Float, // Magnitude of acceleration (minus gravity)
    val timestamp: Long
)

@Singleton
class MotionSensorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun getMotionData(): Flow<MotionData> = callbackFlow {
        val listener = object : SensorEventListener {
            private var gravity = floatArrayOf(0f, 0f, 0f)
            private val alpha = 0.8f // Constant for low-pass filter (gravity)

            override fun onSensorChanged(event: SensorEvent) {
                // Low-pass filter to isolate gravity
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                // High-pass filter to remove gravity
                val linearAccelerationX = event.values[0] - gravity[0]
                val linearAccelerationY = event.values[1] - gravity[1]
                val linearAccelerationZ = event.values[2] - gravity[2]

                // Magnitude of movement
                val magnitude = sqrt(
                    linearAccelerationX * linearAccelerationX +
                    linearAccelerationY * linearAccelerationY +
                    linearAccelerationZ * linearAccelerationZ
                )

                trySend(MotionData(magnitude, event.timestamp))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
