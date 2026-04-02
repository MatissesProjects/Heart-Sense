package com.heart.sense.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val liveHr by viewModel.liveHr.collectAsState()
    val isConnected by viewModel.isWatchConnected.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Heart-Sense", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val statusText = when {
                    isConnected && liveHr != null -> "Watch Connected: $liveHr BPM"
                    isConnected -> "Watch Connected"
                    else -> "Watch Disconnected"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Threshold: ${settings.highHrThreshold} bpm")
                Slider(
                    value = settings.highHrThreshold.toFloat(),
                    onValueChange = { viewModel.updateThreshold(it.toInt()) },
                    valueRange = 60f..180f,
                    steps = 24
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sick Mode")
                    Switch(
                        checked = settings.isSickMode,
                        onCheckedChange = { viewModel.toggleSickMode(it) }
                    )
                }
            }
        }

        Button(onClick = { viewModel.testAlert() }) {
            Text("Send Test Alert")
        }

        Divider()
        
        Text("Recent Alerts", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alerts) { alert ->
                AlertItem(alert)
            }
        }
    }
}

@Composable
fun AlertItem(alert: com.heart.sense.data.Alert) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(alert.type, style = MaterialTheme.typography.bodyLarge)
                Text(alert.timestamp.format(formatter), style = MaterialTheme.typography.bodySmall)
            }
            Text("${alert.hr} BPM", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
