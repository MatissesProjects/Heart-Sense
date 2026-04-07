package com.heart.sense.wear.ai

import com.heart.sense.wear.data.Settings
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class MultiModalPoint(
    val hr: Int,
    val hrv: Float,
    val motionScore: Float,
    val ambientNoise: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class MultiModalDataAggregator @Inject constructor() {
    private val buffer = LinkedList<MultiModalPoint>()
    private val MAX_BUFFER_SIZE = 60 // 10 minutes at 1 update per 10s

    fun addPoint(point: MultiModalPoint) {
        buffer.add(point)
        if (buffer.size > MAX_BUFFER_SIZE) {
            buffer.removeFirst()
        }
    }

    /**
     * Prepares a normalized float array for TFLite input.
     * Expected shape: [1, 60, 4] (Time steps, Features)
     */
    fun getNormalizedInput(settings: Settings): FloatArray {
        val input = FloatArray(MAX_BUFFER_SIZE * 4)
        
        // Fill with recent data, pad with zeros if buffer is smaller than MAX_BUFFER_SIZE
        val startIdx = MAX_BUFFER_SIZE - buffer.size
        
        for (i in 0 until buffer.size) {
            val point = buffer[i]
            val baseIdx = (startIdx + i) * 4
            
            // Normalize HR (0-200 -> 0-1)
            input[baseIdx] = (point.hr.toFloat() / 200f).coerceIn(0f, 1f)
            
            // Normalize HRV (0-100 -> 0-1)
            input[baseIdx + 1] = (point.hrv / 100f).coerceIn(0f, 1f)
            
            // Motion Score is already 0-1
            input[baseIdx + 2] = point.motionScore.coerceIn(0f, 1f)
            
            // Normalize Noise (30-100 dB -> 0-1)
            input[baseIdx + 3] = ((point.ambientNoise - 30f) / 70f).coerceIn(0f, 1f)
        }
        
        return input
    }

    fun isReady(): Boolean = buffer.size >= 10 // Need at least some history
}
