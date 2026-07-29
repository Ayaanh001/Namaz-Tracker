package com.hussain.namaztracker.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hussain.namaztracker.data.BackupManager
import com.hussain.namaztracker.data.SettingsManager
import com.hussain.namaztracker.notifications.AlarmScheduler
import com.hussain.namaztracker.notifications.NotificationHelper
import com.hussain.namaztracker.ui.components.*
import com.hussain.namaztracker.ui.theme.NamazTrackerTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }
    val backupManager = remember { BackupManager(context) }

    val isReminderEnabled by settingsManager.isReminderEnabled.collectAsState(initial = false)
    val reminderHour by settingsManager.reminderHour.collectAsState(initial = 20)
    val reminderMinute by settingsManager.reminderMinute.collectAsState(initial = 0)
    val themeMode by settingsManager.themeMode.collectAsState(initial = ThemeMode.AUTO)

    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                settingsManager.setReminderEnabled(true)
                alarmScheduler.scheduleDailyReminder(reminderHour, reminderMinute)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = backupManager.exportData(it)
                if (result.isSuccess) {
                    Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = backupManager.importData(it)
                if (result.isSuccess) {
                    Toast.makeText(context, "Imported ${result.getOrNull()} records", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Import failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding() + 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            item {
                Column {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ThemeCard(
                        selectedTheme = themeMode,
                        onThemeChange = { mode ->
                            scope.launch { settingsManager.setThemeMode(mode) }
                        }
                    )
                }
            }

            // Notifications Section
            item {
                Column {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        val firstShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                        val lastShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                        val singleShape = RoundedCornerShape(24.dp)

                        SettingTile(
                            title = "Daily Reminder",
                            subtitle = "Remind me to mark my prayers",
                            icon = Icons.Default.Notifications,
                            iconColor = Color(0xFFFF9800),
                            checked = isReminderEnabled,
                            onCheckedChange = { enabled ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        scope.launch {
                                            settingsManager.setReminderEnabled(true)
                                            alarmScheduler.scheduleDailyReminder(reminderHour, reminderMinute)
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        settingsManager.setReminderEnabled(false)
                                        alarmScheduler.cancelReminder()
                                    }
                                }
                            },
                            shape = if (isReminderEnabled) firstShape else singleShape
                        )
                        
                        if (isReminderEnabled) {
                            ClickableTile(
                                title = "Reminder Time",
                                subtitle = String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute),
                                icon = Icons.Default.Schedule,
                                iconColor = Color(0xFF9C27B0),
                                onClick = { showTimePicker = true },
                                shape = lastShape
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            NotificationHelper(context).showNotification()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Send Test Notification")
                    }
                }
            }

            // Data Management Section
            item {
                Column {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        val firstShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                        val lastShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)

                        ClickableTile(
                            title = "Export Data",
                            subtitle = "Save your prayer history to a file",
                            icon = Icons.Default.FileUpload,
                            iconColor = Color(0xFF2196F3),
                            onClick = {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss")
                                val currentDateTime = LocalDateTime.now().format(formatter)
                                val fileName = "Namaz Tracker Backup $currentDateTime.json"
                                exportLauncher.launch(fileName)
                            },
                            shape = firstShape
                        )
                        ClickableTile(
                            title = "Import Data",
                            subtitle = "Restore prayer history from a backup file",
                            icon = Icons.Default.FileDownload,
                            iconColor = Color(0xFF8BC34A),
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            shape = lastShape
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            is24Hour = false
        )
        var isInputMode by remember { mutableStateOf(false) }
        
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showTimePicker = false }
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        text = "Select time",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    if (isInputMode) {
                        TimeInput(state = timeState)
                    } else {
                        TimePicker(state = timeState)
                    }
                    
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isInputMode = !isInputMode }) {
                            Icon(
                                imageVector = if (isInputMode) Icons.Default.Schedule else Icons.Default.Keyboard,
                                contentDescription = "Switch Input Mode"
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = {
                            scope.launch {
                                settingsManager.setReminderTime(timeState.hour, timeState.minute)
                                if (isReminderEnabled) {
                                    alarmScheduler.scheduleDailyReminder(timeState.hour, timeState.minute)
                                }
                            }
                            showTimePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    NamazTrackerTheme {
        SettingsScreen()
    }
}
