package com.heart.sense.wear.ai

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

data class PredictionResult(
    val futureStressScore: Float, // 0.0 to 1.0
    val confidence: Float
)

@Singleton
class StressPredictor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null

    init {
        try {
            // In a real implementation, the model file would be in the assets folder
            // For now, we'll initialize it lazily or handle the missing file gracefully
            val modelBuffer = FileUtil.loadMappedFile(context, "stress_predictor.tflite")
            interpreter = Interpreter(modelBuffer)
            Log.d("StressPredictor", "TFLite model loaded successfully")
        } catch (e: Exception) {
            Log.e("StressPredictor", "Error loading TFLite model: ${e.message}")
        }
    }

    /**
     * Runs inference on the normalized input buffer.
     * @param normalizedInput 1D array of 240 floats (60 steps * 4 features)
     */
    fun predict(normalizedInput: FloatArray): PredictionResult {
        val tflite = interpreter ?: return fallbackPrediction(normalizedInput)

        val inputBuffer = ByteBuffer.allocateDirect(normalizedInput.size * 4).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().put(normalizedInput)
        }

        // Output shape [1, 1] for stress score
        val outputBuffer = ByteBuffer.allocateDirect(4).apply {
            order(ByteOrder.nativeOrder())
        }

        tflite.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        val score = outputBuffer.float

        // Calculate a dummy confidence based on data variance
        val confidence = 0.85f 

        return PredictionResult(score.coerceIn(0f, 1f), confidence)
    }

    /**
     * Simplified heuristic prediction if TFLite model is not yet available.
     */
    private fun fallbackPrediction(input: FloatArray): PredictionResult {
        // Look at the trend of the last 10 points (last 40 floats)
        if (input.size < 40) return PredictionResult(0f, 0f)
        
        var hrSum = 0f
        var hrvSum = 0f
        val steps = 10
        val base = input.size - (steps * 4)
        
        for (i in 0 until steps) {
            hrSum += input[base + (i * 4)]
            hrvSum += input[base + (i * 4) + 1]
        }
        
        val avgHr = hrSum / steps
        val avgHrv = hrvSum / steps
        
        // Predict stress if HR is high AND HRV is low
        val score = (avgHr * 0.7f + (1f - avgHrv) * 0.3f)
        
        return PredictionResult(score.coerceIn(0f, 1f), 0.6f)
    }
}
