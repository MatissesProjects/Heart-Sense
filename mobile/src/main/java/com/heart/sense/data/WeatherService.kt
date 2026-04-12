package com.heart.sense.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse
}

data class WeatherResponse(
    val main: MainWeather,
    val weather: List<WeatherDescription>,
    val name: String
)

data class MainWeather(
    val temp: Float,
    val humidity: Int,
    val pressure: Float
)

data class WeatherDescription(
    val description: String
)

data class AirPollutionResponse(
    val list: List<AirPollutionItem>
)

data class AirPollutionItem(
    val main: AirPollutionMain
)

data class AirPollutionMain(
    val aqi: Int
)
