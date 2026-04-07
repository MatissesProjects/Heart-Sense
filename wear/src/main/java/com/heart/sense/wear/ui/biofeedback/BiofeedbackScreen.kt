package com.heart.sense.wear.ui.biofeedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.heart.sense.wear.util.HapticFeedbackHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BreathPhase { INHALE, HOLD, EXHALE, HOLD_POST }

@Composable
fun BiofeedbackScreen(
    hapticHelper: HapticFeedbackHelper,
    onFinish: () -> Unit
) {
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsLeftInPhase by remember { mutableIntStateOf(4) }
    var isRunning by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Animation for the "Lungs" circle
    val infiniteTransition = rememberInfiniteTransition(label = "lungs")
    val radiusMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                when (phase) {
                    BreathPhase.INHALE -> {
                        hapticHelper.pulseInhale()
                        delay(4000)
                        phase = BreathPhase.HOLD
                    }
                    BreathPhase.HOLD -> {
                        delay(4000)
                        phase = BreathPhase.EXHALE
                    }
                    BreathPhase.EXHALE -> {
                        hapticHelper.pulseExhale()
                        delay(4000)
                        phase = BreathPhase.HOLD_POST
                    }
                    BreathPhase.HOLD_POST -> {
                        delay(4000)
                        phase = BreathPhase.INHALE
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background Circle (Max capacity)
        Canvas(modifier = Modifier.size(140.dp)) {
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = size.minDimension / 2,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Lung Circle (Breathing)
        val color = when (phase) {
            BreathPhase.INHALE -> Color.Cyan
            BreathPhase.HOLD, BreathPhase.HOLD_POST -> Color.Yellow
            BreathPhase.EXHALE -> Color.Green
        }
        
        Canvas(modifier = Modifier.size(140.dp)) {
            drawCircle(
                color = color,
                radius = (size.minDimension / 2) * if (phase == BreathPhase.INHALE || phase == BreathPhase.HOLD) radiusMultiplier else (1.3f - radiusMultiplier)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = phase.name,
                style = MaterialTheme.typography.title1,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { 
                hapticHelper.stop()
                onFinish()
            }) {
                Text("End")
            }
        }
    }
}
