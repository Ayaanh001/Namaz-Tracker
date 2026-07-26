package com.hussain.namaztracker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Salah : Screen("salah", "Salah", Icons.Default.List)
    object Stats : Screen("stats", "Stats", Icons.Default.DateRange)
    object Qibla : Screen("qibla", "Qibla", Icons.Default.Explore)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Salah,
    Screen.Stats,
    Screen.Qibla,
    Screen.Settings
)
