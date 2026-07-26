package com.hussain.namaztracker.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hussain.namaztracker.ui.theme.*

enum class PrayerStatus(val color: Color, val icon: ImageVector?) {
    MISSED(StatusMissed, Icons.Default.Block),
    LATE(StatusLate, Icons.Default.History),
    ALONE(StatusAlone, Icons.Default.Person),
    GROUP(StatusGroup, Icons.Default.Groups),
    UPCOMING(StatusUpcoming, null)
}

data class PrayerEntry(val name: String, val status: PrayerStatus, val icon: ImageVector)
