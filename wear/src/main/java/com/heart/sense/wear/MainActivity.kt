package com.heart.sense.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.MaterialTheme
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
            val measureFlow = remember { healthServicesRepository.getMeasureData(DataType.HEART_RATE_BPM) }
            val measureData by measureFlow.collectAsState(initial = null)
            
            val settings by settingsDataStore.settings.collectAsState(initial = com.heart.sense.wear.data.Settings())
            
            val lastDataPoint = measureData?.getData(DataType.HEART_RATE_BPM)?.lastOrNull()
            val currentHr = lastDataPoint?.value?.toInt()
            
            var isStreaming by remember { mutableStateOf(false) }

            LaunchedEffect(currentHr, isStreaming) {
                if (isStreaming && currentHr != null) {
                    wearableCommunicationRepository.sendLiveHr(currentHr)
                }
            }

            HeartSenseTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HR: ${currentHr ?: "--"} BPM", style = MaterialTheme.typography.title1)
                        Text("Threshold: ${settings.highHrThreshold} BPM", style = MaterialTheme.typography.body2)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Stream", style = MaterialTheme.typography.caption2)
                            Switch(
                                checked = isStreaming,
                                onCheckedChange = { isStreaming = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

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
