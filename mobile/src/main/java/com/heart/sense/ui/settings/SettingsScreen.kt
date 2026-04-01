package com.heart.sense.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Heart-Sense Settings", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("High HR Threshold: ${settings.highHrThreshold} bpm")
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
