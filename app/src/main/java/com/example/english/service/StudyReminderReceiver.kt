package com.example.english.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.english.util.AppLogger
import com.example.english.util.NotificationHelper

/**
 * 学习提醒广播接收器
 * 接收定时提醒的广播，并显示通知
 */
class StudyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STUDY_REMINDER = "com.example.english.STUDY_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        AppLogger.d("Received study reminder broadcast")

        when (intent.action) {
            ACTION_STUDY_REMINDER -> {
                // 显示学习提醒通知
                NotificationHelper.showStudyNotification(context)

                // 记录提醒事件
                recordReminderEvent(context)

                AppLogger.d("Study notification shown")

                // 重新调度明天的提醒
                rescheduleNextReminder(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // 设备重启后重新设置提醒
                AppLogger.d("Device rebooted, need to reschedule reminders")
                rescheduleAfterReboot(context)
            }
        }
    }

    /**
     * 重新调度下一次提醒（第二天同一时间）
     */
    private fun rescheduleNextReminder(context: Context) {
        try {
            val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            val reminderEnabled = prefs.getBoolean("study_reminder_enabled", false)

            if (!reminderEnabled) {
                AppLogger.d("Reminder disabled, not rescheduling")
                return
            }

            val reminderTimeMillis = prefs.getLong("reminder_time_millis", 0L)
            if (reminderTimeMillis <= 0) {
                AppLogger.w("No reminder time set, cannot reschedule")
                return
            }

            // 计算明天同一时间
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = reminderTimeMillis
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }

            val nextTimeMillis = calendar.timeInMillis

            // 重新调度
            val reminderManager = com.example.english.util.ReminderManager(context)
            val requestCode = reminderManager.scheduleNotificationReminder(nextTimeMillis)

            if (requestCode > 0) {
                // 更新存储的 requestCode
                prefs.edit().putInt("alarm_request_code", requestCode).apply()
                AppLogger.d("Next reminder scheduled for: ${java.util.Date(nextTimeMillis)}")
            } else {
                AppLogger.e("Failed to reschedule next reminder")
            }
        } catch (e: Exception) {
            AppLogger.e("Error rescheduling next reminder", e)
        }
    }

    /**
     * 设备重启后重新设置提醒
     */
    private fun rescheduleAfterReboot(context: Context) {
        try {
            val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            val reminderEnabled = prefs.getBoolean("study_reminder_enabled", false)

            if (!reminderEnabled) {
                AppLogger.d("Reminder disabled, not rescheduling after reboot")
                return
            }

            val reminderTimeMillis = prefs.getLong("reminder_time_millis", 0L)
            if (reminderTimeMillis <= 0) {
                AppLogger.w("No reminder time set")
                return
            }

            // 计算下次提醒时间
            val reminderManager = com.example.english.util.ReminderManager(context)
            val (hour, minute) = extractHourMinute(reminderTimeMillis)
            val nextTimeMillis = reminderManager.calculateNextReminderTime(hour, minute)

            // 重新调度
            val requestCode = reminderManager.scheduleNotificationReminder(nextTimeMillis)

            if (requestCode > 0) {
                prefs.edit().putInt("alarm_request_code", requestCode).apply()
                AppLogger.d("Reminder rescheduled after reboot: ${java.util.Date(nextTimeMillis)}")
            }
        } catch (e: Exception) {
            AppLogger.e("Error rescheduling after reboot", e)
        }
    }

    /**
     * 从时间戳中提取小时和分钟
     */
    private fun extractHourMinute(timeMillis: Long): Pair<Int, Int> {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }
        return Pair(
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE)
        )
    }

    /**
     * 记录提醒事件（用于统计）
     */
    private fun recordReminderEvent(context: Context) {
        try {
            val prefs = context.getSharedPreferences("study_stats", Context.MODE_PRIVATE)
            val count = prefs.getInt("reminder_count", 0)
            val lastReminderTime = System.currentTimeMillis()

            prefs.edit()
                .putInt("reminder_count", count + 1)
                .putLong("last_reminder_time", lastReminderTime)
                .apply()

            AppLogger.d("Reminder event recorded: count=${count + 1}")
        } catch (e: Exception) {
            AppLogger.e("Failed to record reminder event", e)
        }
    }
}

