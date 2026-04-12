package com.heart.sense.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heart.sense.data.DailyAverage
import com.heart.sense.data.Settings
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.format.DateTimeFormatter

@Composable
fun HealthDashboard(
    dailyAverages: List<DailyAverage>,
    medicationIntakes: List<com.heart.sense.data.db.MedicationIntake>,
    bloodGlucose: List<com.heart.sense.data.db.BloodGlucose>,
    environmentalInsights: List<com.heart.sense.data.EnvironmentalInsight> = emptyList(),
    cbtEntries: List<com.heart.sense.data.db.CbtJournalEntry> = emptyList(),
    settings: Settings,
    modifier: Modifier = Modifier
) {
    if (dailyAverages.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No data available yet", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(modifier = modifier.padding(16.dp)) {
        if (environmentalInsights.isNotEmpty()) {
            Text("Environmental Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            environmentalInsights.forEach { insight ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(insight.triggerType, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(insight.correlationMessage, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Heart Rate (BPM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        HrChart(dailyAverages, settings)
        
        if (medicationIntakes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Medication Logs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            medicationIntakes.forEach { intake ->
                Text(
                    text = "💊 ${intake.medName} (${intake.dose}) at ${java.time.Instant.ofEpochMilli(intake.timestamp).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (bloodGlucose.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Blood Glucose (CGM)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            val latest = bloodGlucose.first()
            Text(
                text = "Latest: ${"%.1f".format(latest.value)} ${latest.unit} at ${java.time.Instant.ofEpochMilli(latest.timestamp).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (latest.value < 4.0 || latest.value > 10.0) Color.Red else Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("HRV (RMSSD ms)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        HrvChart(dailyAverages)

        Spacer(modifier = Modifier.height(24.dp))

        Text("Blood Oxygen (SpO2 %)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Spo2Chart(dailyAverages)

        Spacer(modifier = Modifier.height(24.dp))

        if (cbtEntries.isNotEmpty()) {
            Text("Subjective Reflections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            cbtEntries.take(5).forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.emotion, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text("Stress: ${entry.stressLevel}/10", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(entry.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")), style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(entry.thoughts, style = MaterialTheme.typography.bodySmall)
                        if (!entry.context.isNullOrEmpty()) {
                            Text("Context: ${entry.context}", style = MaterialTheme.typography.labelSmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text("Daily Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        SummaryHeader()
        dailyAverages.reversed().forEach { average ->
            DailyAverageRow(average)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
        }
    }
}

@Composable
fun HrChart(dailyAverages: List<DailyAverage>, settings: Settings) {
    val modelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(dailyAverages) {
        val entries = dailyAverages.mapIndexed { index, average ->
            FloatEntry(index.toFloat(), average.avgHr.toFloat())
        }
        modelProducer.setEntries(entries)
    }

    val thresholdLine = ThresholdLine(
        thresholdValue = settings.highHrThreshold.toFloat(),
        lineComponent = shapeComponent(color = Color.Red.copy(alpha = 0.5f)),
        labelComponent = textComponent(color = Color.Red),
        thresholdLabel = "Threshold"
    )

    Chart(
        chart = lineChart(
            decorations = listOf(thresholdLine)
        ),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                dailyAverages.getOrNull(value.toInt())?.date?.format(DateTimeFormatter.ofPattern("MM/dd")) ?: ""
            }
        ),
        modifier = Modifier.height(200.dp)
    )
}

@Composable
fun HrvChart(dailyAverages: List<DailyAverage>) {
    val modelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(dailyAverages) {
        val entries = dailyAverages.mapIndexed { index, average ->
            FloatEntry(index.toFloat(), average.hrvRmssd)
        }
        modelProducer.setEntries(entries)
    }

    Chart(
        chart = lineChart(),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                dailyAverages.getOrNull(value.toInt())?.date?.format(DateTimeFormatter.ofPattern("MM/dd")) ?: ""
            }
        ),
        modifier = Modifier.height(200.dp)
    )
}

@Composable
fun Spo2Chart(dailyAverages: List<DailyAverage>) {
    val modelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(dailyAverages) {
        val entries = dailyAverages.mapIndexed { index, average ->
            FloatEntry(index.toFloat(), average.avgSpo2 ?: 0f)
        }
        modelProducer.setEntries(entries)
    }

    Chart(
        chart = lineChart(),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                dailyAverages.getOrNull(value.toInt())?.date?.format(DateTimeFormatter.ofPattern("MM/dd")) ?: ""
            }
        ),
        modifier = Modifier.height(200.dp)
    )
}

@Composable
fun SummaryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Date", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("HR", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold)
        Text("HRV", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold)
        Text("SpO2", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold)
        Text("Status", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyAverageRow(average: DailyAverage) {
    val backgroundColor = if (average.isAlertTriggered) Color.Red.copy(alpha = 0.1f) else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(average.date.format(DateTimeFormatter.ofPattern("MMM dd")), modifier = Modifier.weight(1f))
        Text("${average.avgHr}", modifier = Modifier.weight(0.7f))
        Text("${average.hrvRmssd.toInt()}", modifier = Modifier.weight(0.7f))
        Text("${average.avgSpo2?.toInt() ?: "--"}%", modifier = Modifier.weight(0.7f))
        
        val statusText = if (average.isAlertTriggered) {
            average.alertType ?: "Alert"
        } else {
            "Normal"
        }
        val statusColor = if (average.isAlertTriggered) Color.Red else Color.Green
        
        Text(
            statusText,
            modifier = Modifier.weight(1.1f),
            color = statusColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
