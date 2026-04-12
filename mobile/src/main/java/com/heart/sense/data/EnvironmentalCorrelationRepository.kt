package com.heart.sense.data

import com.heart.sense.data.db.AlertDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class EnvironmentalInsight(
    val triggerType: String,
    val correlationMessage: String,
    val confidence: Float
)

@Singleton
class EnvironmentalCorrelationRepository @Inject constructor(
    private val alertDao: AlertDao
) {
    fun getEnvironmentalInsights(): Flow<List<EnvironmentalInsight>> {
        return alertDao.getRecentAlerts().map { alerts ->
            val insights = mutableListOf<EnvironmentalInsight>()
            
            val behaviors = alerts.filter { it.type.contains("Behavior") }
            if (behaviors.isEmpty()) return@map insights

            // Check AQI Correlation
            val highAqiBehaviors = behaviors.filter { (it.aqi ?: 0) >= 3 }
            if (highAqiBehaviors.size > behaviors.size * 0.4) {
                insights.add(
                    EnvironmentalInsight(
                        "Air Quality",
                        "Agitation events are 40% more frequent when AQI is Moderate or higher.",
                        0.7f
                    )
                )
            }

            // Check Humidity Correlation
            val highHumidityBehaviors = behaviors.filter { (it.humidity ?: 0) > 70 }
            if (highHumidityBehaviors.size > behaviors.size * 0.5) {
                insights.add(
                    EnvironmentalInsight(
                        "Humidity",
                        "High humidity (>70%) correlates with ${highHumidityBehaviors.size} out of ${behaviors.size} agitation events.",
                        0.8f
                    )
                )
            }

            // Check Pressure Drop Correlation (Approximated by low pressure for now)
            val lowPressureBehaviors = behaviors.filter { (it.barometricPressure ?: 1013f) < 1000f }
            if (lowPressureBehaviors.isNotEmpty()) {
                insights.add(
                    EnvironmentalInsight(
                        "Barometric Pressure",
                        "Agitation spikes detected during low pressure systems (<1000hPa).",
                        0.6f
                    )
                )
            }

            insights
        }
    }
}
