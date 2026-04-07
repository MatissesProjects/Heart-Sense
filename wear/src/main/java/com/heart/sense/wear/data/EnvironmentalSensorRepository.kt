package com.heart.sense.wear.data

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
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class EnvironmentalData(
    val lux: Float, // Ambient Light
    val decibels: Int // Ambient Noise (Estimated)
)

@Singleton
class EnvironmentalSensorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getLightData(): Flow<Float> = callbackFlow {
        if (lightSensor == null) {
            trySend(0f)
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event.values[0])
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    /**
     * Estimates decibels using MediaRecorder. 
     * Note: Requires RECORD_AUDIO permission.
     */
    fun getNoiseData(): Flow<Int> = callbackFlow {
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
            Log.e("EnvSensor", "Failed to start MediaRecorder: ${e.message}")
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
                    Log.e("EnvSensor", "Error reading amplitude: ${e.message}")
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
