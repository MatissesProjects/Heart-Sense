package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "fhir_export_logs")
data class FhirExportLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val visitId: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: String, // "SUCCESS", "FAILED"
    val resourceCount: Int,
    val errorMessage: String? = null
)
