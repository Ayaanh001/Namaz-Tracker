package com.hussain.namaztracker.models

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "prayer_records", primaryKeys = ["date", "prayerName"])
data class PrayerRecord(
    val date: String, // ISO-8601 format: YYYY-MM-DD
    val prayerName: String,
    val status: String
)
