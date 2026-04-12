package com.heart.sense.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmbientSensorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Provides a flow of ambient temperature updates in Celsius.
     */
    fun getAmbientTemp(): Flow<Float> = callbackFlow {
        if (tempSensor == null) {
            trySend(20f) // Fallback for testing/unsupported devices
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
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /**
     * Provides a flow of ambient light updates in Lux.
     */
    fun getAmbientLux(): Flow<Float> = callbackFlow {
        if (lightSensor == null) {
            trySend(0f)
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    trySend(event.values[0])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /**
     * Estimates decibels using MediaRecorder.
     */
    fun getAmbientNoise(): Flow<Int> = callbackFlow {
        val recorder = try {
            MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AmbientSensor", "Failed to start MediaRecorder: ${e.message}")
            null
        }

        if (recorder == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val job = repositoryScope.launch {
            while (true) {
                try {
                    val amplitude = recorder.maxAmplitude
                    if (amplitude > 0) {
                        val db = (20 * Math.log10(amplitude.toDouble())).toInt()
                        trySend(db)
                    }
                } catch (e: Exception) {
                    Log.e("AmbientSensor", "Error reading amplitude: ${e.message}")
                }
                delay(1000)
            }
        }

        awaitClose {
            job.cancel()
            try {
                recorder.stop()
                recorder.release()
            } catch (e: Exception) {}
        }
    }
}
