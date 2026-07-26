package com.hussain.namaztracker.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material.icons.outlined.WbSunny
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hussain.namaztracker.data.PrayerDatabase
import com.hussain.namaztracker.models.PrayerEntry
import com.hussain.namaztracker.models.PrayerRecord
import com.hussain.namaztracker.models.PrayerStatus
import com.hussain.namaztracker.models.StatsRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StatsInsights(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val breakdown: Map<PrayerStatus, Int> = emptyMap()
)

class SalahViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val prayerDao = PrayerDatabase.getDatabase(application).prayerDao()

    private val _prayerData = MutableStateFlow<Map<LocalDate, List<PrayerEntry>>>(emptyMap())
    val prayerData: StateFlow<Map<LocalDate, List<PrayerEntry>>> = _prayerData.asStateFlow()

    private val _selectedRange = MutableStateFlow(StatsRange.ALL_TIME)
    val selectedRange: StateFlow<StatsRange> = _selectedRange.asStateFlow()

    val allHistory: StateFlow<List<PrayerRecord>> = prayerDao.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val statsInsights: StateFlow<StatsInsights> = combine(allHistory, _selectedRange) { records, range ->
        calculateInsights(records, range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StatsInsights()
    )

    fun setStatsRange(range: StatsRange) {
        _selectedRange.value = range
    }

    private fun calculateInsights(records: List<PrayerRecord>, range: StatsRange): StatsInsights {
        if (records.isEmpty()) return StatsInsights()

        val historyMap = records.groupBy { it.date }
            .mapValues { entry -> entry.value.size } 

        val today = LocalDate.now()
        
        // A day is "complete" if 5 prayers are recorded
        val completeDates = historyMap.filter { it.value == 5 }.keys.map { LocalDate.parse(it) }.toSet()
        
        // Current Streak (starting from today or yesterday)
        var currentStreak = 0
        var checkDate = today
        
        // If today isn't complete, check if yesterday was. If neither, streak is 0.
        if (!completeDates.contains(checkDate)) {
            checkDate = today.minusDays(1)
        }
        
        while (completeDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }
        
        // Best Streak
        val sortedDates = completeDates.sorted()
        var bestStreak = 0
        if (sortedDates.isNotEmpty()) {
            var currentTemp = 1
            bestStreak = 1
            for (i in 1 until sortedDates.size) {
                if (sortedDates[i] == sortedDates[i-1].plusDays(1)) {
                    currentTemp++
                } else {
                    currentTemp = 1
                }
                if (currentTemp > bestStreak) bestStreak = currentTemp
            }
        }
        
        // Breakdown based on range
        val filteredRecords = when (range) {
            StatsRange.WEEKS -> {
                val weekAgo = today.minusDays(7)
                records.filter { !LocalDate.parse(it.date).isBefore(weekAgo) }
            }
            StatsRange.MONTHS -> {
                val monthAgo = today.minusDays(30)
                records.filter { !LocalDate.parse(it.date).isBefore(monthAgo) }
            }
            StatsRange.YEARS -> {
                val yearAgo = today.minusDays(365)
                records.filter { !LocalDate.parse(it.date).isBefore(yearAgo) }
            }
            StatsRange.ALL_TIME -> records
        }

        val breakdown = filteredRecords.groupBy { PrayerStatus.valueOf(it.status) }
            .mapValues { it.value.size }

        return StatsInsights(currentStreak, bestStreak, breakdown)
    }

    init {
        // Load all data from DB on start? Or just observe?
        // Let's observe the whole DB or just current visible days.
        // For simplicity, we can load a range of dates or load as needed.
        // Let's load data as it's requested or just observe.
    }

    private fun getDefaultPrayers(): List<PrayerEntry> {
        return listOf(
            PrayerEntry("Fajr", PrayerStatus.UPCOMING, Icons.Outlined.Brightness2),
            PrayerEntry("Dhuhr", PrayerStatus.UPCOMING, Icons.Outlined.WbSunny),
            PrayerEntry("Asr", PrayerStatus.UPCOMING, Icons.Outlined.Brightness7),
            PrayerEntry("Maghrib", PrayerStatus.UPCOMING, Icons.Outlined.Brightness5),
            PrayerEntry("Isha", PrayerStatus.UPCOMING, Icons.Outlined.Brightness2)
        )
    }

    fun getPrayersForDate(date: LocalDate): List<PrayerEntry> {
        // If not already observing this date, we should start.
        loadDateIfNeeded(date)
        return _prayerData.value[date] ?: getDefaultPrayers()
    }

    private val observedDates = mutableSetOf<LocalDate>()

    private fun loadDateIfNeeded(date: LocalDate) {
        if (observedDates.contains(date)) return
        observedDates.add(date)
        
        viewModelScope.launch {
            prayerDao.getRecordsForDate(date.toString()).collectLatest { records ->
                val defaultPrayers = getDefaultPrayers()
                val updatedPrayers = defaultPrayers.map { defaultPrayer ->
                    val record = records.find { it.prayerName == defaultPrayer.name }
                    if (record != null) {
                        defaultPrayer.copy(status = PrayerStatus.valueOf(record.status))
                    } else {
                        defaultPrayer
                    }
                }
                _prayerData.update { it + (date to updatedPrayers) }
            }
        }
    }

    fun updatePrayerStatus(date: LocalDate, prayerName: String, newStatus: PrayerStatus) {
        val currentPrayers = getPrayersForDate(date)
        val updatedStatus = if (currentPrayers.find { it.name == prayerName }?.status == newStatus) {
            PrayerStatus.UPCOMING
        } else {
            newStatus
        }

        // Optimistic UI Update
        _prayerData.update { currentMap ->
            val prayers = currentMap[date] ?: getDefaultPrayers()
            val updatedList = prayers.map { 
                if (it.name == prayerName) it.copy(status = updatedStatus) else it 
            }
            currentMap + (date to updatedList)
        }

        viewModelScope.launch {
            if (updatedStatus == PrayerStatus.UPCOMING) {
                prayerDao.deleteRecord(date.toString(), prayerName)
            } else {
                prayerDao.insertRecord(
                    PrayerRecord(
                        date = date.toString(),
                        prayerName = prayerName,
                        status = updatedStatus.name
                    )
                )
            }
        }
    }

    fun updateAllPrayersStatus(date: LocalDate, newStatus: PrayerStatus) {
        // Toggle logic: If all are already this status, unselect all
        val currentPrayers = getPrayersForDate(date)
        val allAreSame = currentPrayers.all { it.status == newStatus }
        val finalStatus = if (allAreSame) PrayerStatus.UPCOMING else newStatus

        // Optimistic UI Update
        _prayerData.update { currentMap ->
            val prayers = currentMap[date] ?: getDefaultPrayers()
            val updatedList = prayers.map { it.copy(status = finalStatus) }
            currentMap + (date to updatedList)
        }

        val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        viewModelScope.launch {
            prayerNames.forEach { name ->
                if (finalStatus == PrayerStatus.UPCOMING) {
                    prayerDao.deleteRecord(date.toString(), name)
                } else {
                    prayerDao.insertRecord(
                        PrayerRecord(
                            date = date.toString(),
                            prayerName = name,
                            status = finalStatus.name
                        )
                    )
                }
            }
        }
    }
}
