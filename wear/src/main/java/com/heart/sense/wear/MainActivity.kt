package com.heart.sense.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.lifecycle.lifecycleScope
import androidx.health.services.client.data.DataType
import com.heart.sense.wear.data.WearableCommunicationRepository
import com.heart.sense.wear.data.HealthServicesRepository
import com.heart.sense.wear.service.PassiveMonitoringService
import com.heart.sense.wear.ui.theme.HeartSenseTheme
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
            
            val settings by settingsDataStore.settings.collectAsState(initial = com.heart.sense.wear.data.Settings())
            
            val currentHr = when (val update = measureUpdate) {
                is com.heart.sense.wear.data.MeasureUpdate.DataReceived -> {
                    update.container.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value?.toInt()
                }
                else -> null
            }
            
            var isStreaming by remember { mutableStateOf(false) }

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
                                decreaseIcon = { Icon(android.R.drawable.ic_media_previous, "Decrease") },
                                increaseIcon = { Icon(android.R.drawable.ic_media_next, "Increase") }
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
                                color = if (isSnoozed) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isSnoozed,
                                onCheckedChange = { 
                                    scope.launch { 
                                        if (it) settingsRepository.setSnooze(30) else settingsRepository.updateThreshold(settings.highHrThreshold) // Toggle clear via threshold sync
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
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
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
