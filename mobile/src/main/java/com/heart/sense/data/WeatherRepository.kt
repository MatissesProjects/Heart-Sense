package com.heart.sense.data

import android.util.Log
import com.heart.sense.data.db.EnvironmentalContext
import com.heart.sense.data.db.EnvironmentalContextDao
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val environmentalContextDao: EnvironmentalContextDao,
    private val locationRepository: LocationRepository
) {
    // API Key should be injected or from BuildConfig in real apps.
    // For now, using a placeholder.
    private val apiKey = "YOUR_OPENWEATHER_API_KEY"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    suspend fun fetchAndStoreEnvironmentalContext() {
        val location = locationRepository.getCurrentLocation() ?: run {
            Log.e("WeatherRepository", "Cannot fetch weather: Location is null")
            return
        }

        try {
            val weather = weatherService.getCurrentWeather(location.latitude, location.longitude, apiKey)
            val pollution = weatherService.getAirPollution(location.latitude, location.longitude, apiKey)

            val context = EnvironmentalContext(
                aqi = pollution.list.firstOrNull()?.main?.aqi ?: 0,
                temperature = weather.main.temp,
                humidity = weather.main.humidity,
                pressure = weather.main.pressure,
                description = weather.weather.firstOrNull()?.description
            )

            environmentalContextDao.insert(context)
            Log.d("WeatherRepository", "Stored new environmental context: $context")
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching environmental data: ${e.message}")
        }
    }

    fun getLatestEnvironmentalContext(): Flow<EnvironmentalContext?> = 
        environmentalContextDao.getLatest()
}
