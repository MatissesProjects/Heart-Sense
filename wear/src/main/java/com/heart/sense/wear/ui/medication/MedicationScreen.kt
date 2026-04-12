package com.heart.sense.wear.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items

@Composable
fun MedicationScreen(
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        autoCentering = true
    ) {
        item {
            ListHeader {
                Text("Medications")
            }
        }

        if (medications.isEmpty()) {
            item {
                Text(
                    text = "No medications synced from phone.",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(medications) { med ->
                Chip(
                    onClick = { viewModel.logIntake(med) },
                    label = { Text(med.name) },
                    secondaryLabel = { Text(med.dose) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
