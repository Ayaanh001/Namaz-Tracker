package com.hussain.namaztracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hussain.namaztracker.data.SettingsManager
import com.hussain.namaztracker.notifications.NotificationHelper
import com.hussain.namaztracker.ui.components.FloatingBottomNavigation
import com.hussain.namaztracker.ui.components.ThemeMode
import com.hussain.namaztracker.ui.screens.QiblaScreen
import com.hussain.namaztracker.ui.screens.SalahScreen
import com.hussain.namaztracker.ui.screens.SalahViewModel
import com.hussain.namaztracker.ui.screens.SettingsScreen
import com.hussain.namaztracker.ui.screens.StatsScreen
import com.hussain.namaztracker.ui.theme.NamazTrackerTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Ensure notification channel is created
        NotificationHelper(this).createNotificationChannel()
        
        enableEdgeToEdge()
        setContent {
            val settingsManager = remember { SettingsManager(applicationContext) }
            val themeMode by settingsManager.themeMode.collectAsState(initial = ThemeMode.AUTO)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AUTO -> isSystemInDarkTheme()
            }

            NamazTrackerTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val sharedViewModel: SalahViewModel = viewModel()
                val hazeState = remember { HazeState() }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = MaterialTheme.colorScheme.background
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Salah.route,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .hazeSource(state = hazeState)
                            ) {
                                composable(Screen.Salah.route) { 
                                    SalahScreen(viewModel = sharedViewModel) 
                                }
                                composable(Screen.Stats.route) { 
                                    StatsScreen(viewModel = sharedViewModel) 
                                }
                                composable(Screen.Qibla.route) { 
                                    QiblaScreen() 
                                }
                                composable(Screen.Settings.route) { 
                                    SettingsScreen() 
                                }
                            }
                        }

                        // Custom Floating Bottom Navigation
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val currentRoute = currentDestination?.route

                        FloatingBottomNavigation(
                            screens = bottomNavItems,
                            currentRoute = currentRoute,
                            hazeState = hazeState,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
