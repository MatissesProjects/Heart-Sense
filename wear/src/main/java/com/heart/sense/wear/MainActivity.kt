package com.heart.sense.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.focusable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.*
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.lifecycle.lifecycleScope
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataPoint
import com.heart.sense.wear.data.WearableCommunicationRepository
import com.heart.sense.wear.data.HealthServicesRepository
import com.heart.sense.wear.data.Settings
import com.heart.sense.wear.data.AdvancedSensorRepository
import com.heart.sense.wear.service.PassiveMonitoringService
import com.heart.sense.wear.ui.theme.HeartSenseTheme
import com.heart.sense.wear.ui.biofeedback.BiofeedbackScreen
import com.heart.sense.wear.util.HapticFeedbackHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var wearableCommunicationRepository: WearableCommunicationRepository

    @Inject
    lateinit var healthServicesRepository: HealthServicesRepository

    @Inject
    lateinit var settingsDataStore: com.heart.sense.wear.data.SettingsDataStore

    @Inject
    lateinit var settingsRepository: com.heart.sense.wear.data.SettingsRepository

    @Inject
    lateinit var hapticHelper: HapticFeedbackHelper

    @Inject
    lateinit var advancedSensorRepository: AdvancedSensorRepository

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            startMonitoring()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()

        setContent {
            val scope = rememberCoroutineScope()
            val listState = rememberScalingLazyListState()
            val focusRequester = remember { FocusRequester() }
            
            val measureFlow = remember { healthServicesRepository.getMeasureData(DataType.HEART_RATE_BPM) }
            val measureUpdate by measureFlow.collectAsState(initial = null)
            
            val settings by settingsDataStore.settings.collectAsState(initial = Settings())
            
            val currentHr = when (val update = measureUpdate) {
                is com.heart.sense.wear.data.MeasureUpdate.DataReceived -> {
                    val hrDataPoints = update.container.getData(DataType.HEART_RATE_BPM)
                    hrDataPoints.lastOrNull()?.value?.toInt()
                }
                else -> null
            }
            
            var isStreaming by remember { mutableStateOf(false) }
            var showBiofeedback by remember { mutableStateOf(intent.getBooleanExtra("show_biofeedback", false)) }
            var showTempDialog by remember { mutableStateOf(false) }
            var tempInput by remember { mutableFloatStateOf(37.0f) }

            LaunchedEffect(currentHr, isStreaming) {
                if (isStreaming && currentHr != null) {
                    wearableCommunicationRepository.sendLiveHr(currentHr)
                }
            }

            // Request focus for rotary input
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            HeartSenseTheme {
                if (showBiofeedback) {
                    BiofeedbackScreen(
                        hapticHelper = hapticHelper,
                        onFinish = { showBiofeedback = false }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ScalingLazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .onRotaryScrollEvent {
                                    scope.launch {
                                        listState.scrollBy(it.verticalScrollPixels)
                                    }
                                    true
                                }
                                .focusRequester(focusRequester)
                                .focusable(),
                            state = listState,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Text("HR: ${currentHr ?: "--"} BPM", style = MaterialTheme.typography.title1)
                            }

                            if (settings.isCalibrating || settings.isCalibrated) {
                                item {
                                    CalibrationProgressItem(settings)
                                }
                            }
                            
                            item {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Threshold: ${settings.highHrThreshold}", style = MaterialTheme.typography.caption2)
                                    InlineSlider(
                                        value = settings.highHrThreshold.toFloat(),
                                        onValueChange = { 
                                            scope.launch { settingsRepository.updateThreshold(it.toInt()) }
                                        },
                                        valueRange = 60f..180f,
                                        steps = 24,
                                        enabled = !settings.isCalibrating,
                                        decreaseIcon = { Icon(Icons.Default.KeyboardArrowDown, "Decrease") },
                                        increaseIcon = { Icon(Icons.Default.KeyboardArrowUp, "Increase") }
                                    )
                                }
                            }

                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Sick Mode", style = MaterialTheme.typography.caption2)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = settings.isSickMode,
                                        onCheckedChange = { 
                                            scope.launch { settingsRepository.toggleSickMode(it) }
                                        }
                                    )
                                }
                            }

                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isSnoozed = settings.isSnoozed
                                    Text(
                                        if (isSnoozed) "Snoozed (${settings.snoozeRemainingMinutes}m)" else "Snooze",
                                        style = MaterialTheme.typography.caption2,
                                        color = if (isSnoozed) Color.Red else Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = isSnoozed,
                                        onCheckedChange = { 
                                            scope.launch { 
                                                if (it) settingsRepository.setSnooze(30) else settingsRepository.updateThreshold(settings.highHrThreshold)
                                            }
                                        }
                                    )
                                }
                            }

                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Stream", style = MaterialTheme.typography.caption2)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = isStreaming,
                                        onCheckedChange = { isStreaming = it }
                                    )
                                }
                            }

                            if (!settings.isCalibrating && !settings.isCalibrated) {
                                item {
                                    Button(onClick = { 
                                        scope.launch {
                                            settingsDataStore.startCalibration()
                                        }
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Start Calibration", style = MaterialTheme.typography.caption2)
                                    }
                                }
                            }

                            item {
                                Button(onClick = { showBiofeedback = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Calming Exercise", style = MaterialTheme.typography.caption2)
                                }
                            }

                            item {
                                Button(onClick = { showTempDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Manual Temp", style = MaterialTheme.typography.caption2)
                                }
                            }

                            item {
                                Button(onClick = {
                                    currentHr?.let { hr ->
                                        scope.launch {
                                            wearableCommunicationRepository.sendHrAlert(hr)
                                        }
                                    }
                                }) {
                                    Text("Send Alert")
                                }
                            }
                        }

                        Dialog(
                            showDialog = showTempDialog,
                            onDismissRequest = { showTempDialog = false }
                        ) {
                            Alert(
                                title = { Text("Enter Temp (°C)") },
                                positiveButton = {
                                    Button(onClick = { 
                                        advancedSensorRepository.setManualTemp(tempInput)
                                        showTempDialog = false 
                                    }) {
                                        Text("OK")
                                    }
                                },
                                negativeButton = {
                                    Button(onClick = { showTempDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(String.format("%.1f", tempInput))
                                    InlineSlider(
                                        value = tempInput,
                                        onValueChange = { tempInput = it },
                                        valueRange = 35f..42f,
                                        steps = 70,
                                        decreaseIcon = { Icon(Icons.Default.KeyboardArrowDown, "Decrease") },
                                        increaseIcon = { Icon(Icons.Default.KeyboardArrowUp, "Increase") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CalibrationProgressItem(settings: Settings) {
        val durationHours = if (settings.isCalibrating) {
            (System.currentTimeMillis() - settings.calibrationStartTime) / (1000 * 60 * 60)
        } else {
            0L
        }
        val totalHours = 48f
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            if (settings.isCalibrating) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                    CircularProgressIndicator(
                        progress = (durationHours.toFloat() / totalHours).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 3.dp
                    )
                    Text("${(durationHours.toFloat() / totalHours * 100).toInt()}%", style = MaterialTheme.typography.caption3)
                }
                Text("Calibrating (${durationHours}h)", style = MaterialTheme.typography.caption2)
            } else {
                Text("✅ Calibrated", color = Color.Green, style = MaterialTheme.typography.caption1)
                Text("RHR: ${settings.restingHr} BPM", style = MaterialTheme.typography.caption2)
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.RECORD_AUDIO
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        try {
            permissions.add("android.permission.health.READ_HEART_RATE")
        } catch (e: Exception) {}

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        lifecycleScope.launch {
            healthServicesRepository.startPassiveMonitoring(PassiveMonitoringService::class.java)
        }
    }
}
