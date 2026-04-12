package com.heart.sense.ui.cbt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heart.sense.data.Alert
import com.heart.sense.data.db.CbtJournalEntry
import com.heart.sense.ui.settings.SettingsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CbtReflectionScreen(
    alertId: Int,
    viewModel: SettingsViewModel
) {
    var alert by remember { mutableStateOf<Alert?>(null) }
    var emotion by remember { mutableStateOf("") }
    var thoughts by remember { mutableStateOf("") }
    var stressLevel by remember { mutableFloatStateOf(5f) }
    var contextText by remember { mutableStateOf("") }

    val emotions = listOf("Anxious", "Angry", "Overwhelmed", "Sad", "Frustrated", "Tired", "Stressed", "Restless")

    LaunchedEffect(alertId) {
        alert = viewModel.getAlertById(alertId)
    }

    if (alert == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reflection: ${alert?.type}") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "A stress event was detected at ${alert?.timestamp?.format(DateTimeFormatter.ofPattern("HH:mm"))}. Take a moment to check in with yourself.",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text("How were you feeling?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emotions.forEach { e ->
                    FilterChip(
                        selected = emotion == e,
                        onClick = { emotion = e },
                        label = { Text(e) }
                    )
                }
            }

            Text("What was on your mind?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = thoughts,
                onValueChange = { thoughts = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("e.g., I was worried about my meeting...") }
            )

            Text("Rate your stress (1-10)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = stressLevel,
                    onValueChange = { stressLevel = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stressLevel.toInt().toString(),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text("What was happening around you? (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = contextText,
                onValueChange = { contextText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Loud construction outside, busy office...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveCbtEntry(
                        CbtJournalEntry(
                            alertId = alertId,
                            emotion = emotion,
                            thoughts = thoughts,
                            stressLevel = stressLevel.toInt(),
                            context = contextText
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = emotion.isNotEmpty() && thoughts.isNotEmpty()
            ) {
                Text("Save Reflection")
            }
            
            TextButton(
                onClick = { viewModel.dismissCbtReflection() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now")
            }
        }
    }
}
