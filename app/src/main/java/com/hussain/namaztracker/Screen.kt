package com.hussain.namaztracker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Salah : Screen("salah", "Home", Icons.Rounded.Home)
    object Stats : Screen("stats", "Stats", Icons.Rounded.BarChart)
    object Qibla : Screen("qibla", "Qibla", Icons.Rounded.Explore)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
}

val bottomNavItems = listOf(
    Screen.Salah,
    Screen.Stats,
    Screen.Qibla,
    Screen.Settings
)
