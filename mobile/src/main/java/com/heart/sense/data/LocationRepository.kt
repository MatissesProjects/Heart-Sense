package com.heart.sense.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDao: LocationDao
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting location", e)
            null
        }
    }

    suspend fun logLocationTag(alertType: String, intensity: Int, visitId: String? = null) {
        val location = getCurrentLocation()
        if (location != null) {
            val tag = LocationTag(
                timestamp = System.currentTimeMillis(),
                latitude = location.latitude,
                longitude = location.longitude,
                alertType = alertType,
                intensity = intensity,
                visitId = visitId
            )
            locationDao.insertLocationTag(tag)
            Log.d("LocationRepository", "Logged location tag: $tag")
        } else {
            Log.w("LocationRepository", "Could not log location tag: Location is null")
        }
    }

    fun getAllLocationTags(): Flow<List<LocationTag>> = locationDao.getAllLocationTags()
    
    fun getLocationTagsForVisit(visitId: String): Flow<List<LocationTag>> = 
        locationDao.getLocationTagsForVisit(visitId)
}
