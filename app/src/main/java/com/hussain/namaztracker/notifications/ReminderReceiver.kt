package com.hussain.namaztracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hussain.namaztracker.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "onReceive: action=${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule after reboot
            Log.d("ReminderReceiver", "Rescheduling alarms after boot")
            val settingsManager = SettingsManager(context)
            val alarmScheduler = AlarmScheduler(context)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = settingsManager.isReminderEnabled.first()
                if (enabled) {
                    val hour = settingsManager.reminderHour.first()
                    val minute = settingsManager.reminderMinute.first()
                    Log.d("ReminderReceiver", "Scheduling alarm for $hour:$minute")
                    alarmScheduler.scheduleDailyReminder(hour, minute)
                }
            }
        } else {
            // Trigger notification
            Log.d("ReminderReceiver", "Triggering notification")
            val notificationHelper = NotificationHelper(context)
            notificationHelper.createNotificationChannel()
            notificationHelper.showNotification()

            // Reschedule for next day since we are using non-repeating exact alarms
            val settingsManager = SettingsManager(context)
            val alarmScheduler = AlarmScheduler(context)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = settingsManager.isReminderEnabled.first()
                if (enabled) {
                    val hour = settingsManager.reminderHour.first()
                    val minute = settingsManager.reminderMinute.first()
                    Log.d("ReminderReceiver", "Rescheduling next alarm for $hour:$minute")
                    alarmScheduler.scheduleDailyReminder(hour, minute)
                }
            }
        }
    }
}
