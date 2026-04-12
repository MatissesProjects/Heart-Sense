package com.heart.sense.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "cbt_journal_entries")
data class CbtJournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alertId: Int, // Link to the specific Alert
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val emotion: String, // e.g., "Anxious", "Angry", "Overwhelmed"
    val thoughts: String, // "What were you thinking?"
    val stressLevel: Int, // 1-10
    val context: String? = null // "What was happening?"
)
