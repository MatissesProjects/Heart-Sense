package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FhirExportLogDao {
    @Insert
    suspend fun insert(log: FhirExportLog)

    @Query("SELECT * FROM fhir_export_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FhirExportLog>>

    @Query("SELECT * FROM fhir_export_logs WHERE visitId = :visitId ORDER BY timestamp DESC")
    fun getLogsForVisit(visitId: String): Flow<List<FhirExportLog>>
}
