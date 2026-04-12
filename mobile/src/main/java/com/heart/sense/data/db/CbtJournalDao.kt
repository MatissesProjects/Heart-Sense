package com.heart.sense.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CbtJournalDao {
    @Insert
    suspend fun insert(entry: CbtJournalEntry)

    @Query("SELECT * FROM cbt_journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<CbtJournalEntry>>

    @Query("SELECT * FROM cbt_journal_entries WHERE alertId = :alertId LIMIT 1")
    suspend fun getEntryForAlert(alertId: Int): CbtJournalEntry?

    @Query("DELETE FROM cbt_journal_entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)
}
