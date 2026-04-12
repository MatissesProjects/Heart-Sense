package com.heart.sense.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocationTag(tag: LocationTag)

    @Query("SELECT * FROM location_tags ORDER BY timestamp DESC")
    fun getAllLocationTags(): Flow<List<LocationTag>>

    @Query("SELECT * FROM location_tags WHERE visitId = :visitId")
    fun getLocationTagsForVisit(visitId: String): Flow<List<LocationTag>>
    
    @Query("DELETE FROM location_tags")
    suspend fun clearAll()
}
