package com.hussain.namaztracker.data

import androidx.room.*
import com.hussain.namaztracker.models.PrayerRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<PrayerRecord>>

    @Query("SELECT * FROM prayer_records")
    fun getAllRecords(): Flow<List<PrayerRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PrayerRecord)

    @Query("DELETE FROM prayer_records WHERE date = :date AND prayerName = :prayerName")
    suspend fun deleteRecord(date: String, prayerName: String)
}
