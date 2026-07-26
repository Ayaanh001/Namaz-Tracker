package com.hussain.namaztracker.data

import android.content.Context
import android.net.Uri
import com.hussain.namaztracker.models.PrayerRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class BackupManager(private val context: Context) {
    private val prayerDao = PrayerDatabase.getDatabase(context).prayerDao()
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true 
    }

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val records = prayerDao.getAllRecords().first()
            val jsonString = json.encodeToString(records)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: throw Exception("Failed to open output stream")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                }
            } ?: throw Exception("Failed to open input stream")
            
            val jsonString = stringBuilder.toString()
            val records = json.decodeFromString<List<PrayerRecord>>(jsonString)
            
            records.forEach { record ->
                prayerDao.insertRecord(record)
            }
            
            Result.success(records.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
