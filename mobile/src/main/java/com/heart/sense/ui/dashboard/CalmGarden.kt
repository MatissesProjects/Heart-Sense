package com.heart.sense.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heart.sense.data.Settings

@Composable
fun CalmGardenCard(settings: Settings) {
    val points = settings.calmPoints
    val currentStreak = settings.currentStreakMinutes
    val bestStreak = settings.bestStreakMinutes

    // Simple visual progression: 1 flower for every 100 points
    val flowerCount = (points / 100).coerceAtMost(10)
    val gardenEmojis = StringBuilder().apply {
        repeat(flowerCount) { append("🌸 ") }
        if (flowerCount < 10) repeat(10 - flowerCount) { append("🌱 ") }
    }.toString()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Your Calm Garden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$points pts", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            
            Text(
                text = gardenEmojis,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            LinearProgressIndicator(
                progress = { (points % 100) / 100f },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Streak", style = MaterialTheme.typography.labelSmall)
                    Text("$currentStreak min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Personal Best", style = MaterialTheme.typography.labelSmall)
                    Text("$bestStreak min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
            
            Text(
                "Earn points by keeping your heart rate near baseline. Streaks break if HR stays high for too long.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
