package com.example.english.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import com.example.english.service.StudyReminderReceiver
import java.util.*
import kotlin.random.Random

/**
 * 提醒管理器
 * 负责管理三种提醒方式：通知、日历、闹钟
 */
class ReminderManager(private val context: Context) {

    companion object {
        private const val TAG = "ReminderManager"
        private const val PREFS_NAME = "reminder_prefs"
        private const val KEY_LAST_REQUEST_CODE = "last_request_code"
    }

    private val alarmManager: AlarmManager? by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    }

    // ==================== 通知提醒 ====================

    /**
     * 调度通知提醒
     * @param timeInMillis 提醒时间戳
     * @return 请求码，失败返回 -1
     */
    fun scheduleNotificationReminder(timeInMillis: Long): Int {
        // 检查 AlarmManager 是否可用
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available")
            return -1
        }

        // Android 12+ 检查精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms() != true) {
                Log.w(TAG, "No permission to schedule exact alarms")
                // 仍然尝试使用不精确的闹钟
            }
        }

        val requestCode = generateRequestCode()

        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = StudyReminderReceiver.ACTION_STUDY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager?.canScheduleExactAlarms() == true) {
                    scheduleExactAlarm(timeInMillis, pendingIntent)
                    Log.d(TAG, "Exact alarm scheduled for ${Date(timeInMillis)}")
                } else {
                    scheduleInexactAlarm(timeInMillis, pendingIntent)
                    Log.d(TAG, "Inexact alarm scheduled for ${Date(timeInMillis)} (no exact alarm permission)")
                }
            } else {
                scheduleExactAlarm(timeInMillis, pendingIntent)
                Log.d(TAG, "Notification reminder scheduled for ${Date(timeInMillis)}")
            }

            return requestCode
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied when scheduling reminder", e)
            return -1
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule notification reminder", e)
            return -1
        }
    }

    /**
     * 取消通知提醒
     */
    fun cancelNotificationReminder(requestCode: Int) {
        try {
            val intent = Intent(context, StudyReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager?.cancel(it)
                it.cancel()
                Log.d(TAG, "Notification reminder cancelled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel notification reminder", e)
        }
    }

    // ==================== 日历事件 ====================

    /**
     * 创建日历事件
     * @param timeInMillis 事件时间戳
     * @return 日历事件ID，失败返回null
     */
    fun createCalendarEvent(timeInMillis: Long): Long? {
        // 检查权限
        if (!hasCalendarPermission()) {
            Log.e(TAG, "No calendar permission, cannot create event")
            return null
        }

        try {
            val calendarId = getDefaultCalendarId() ?: run {
                Log.w(TAG, "No calendar found on device")
                return null
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, timeInMillis)
                put(CalendarContract.Events.DTEND, timeInMillis + 30 * 60 * 1000) // 持续30分钟
                put(CalendarContract.Events.TITLE, "📚 英语学习时间")
                put(CalendarContract.Events.DESCRIPTION, "坚持每日学习，提升英语水平")
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.RRULE, "FREQ=DAILY") // 每日重复
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val uri = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                values
            )

            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                // 添加提醒（提前5分钟）
                addEventReminder(eventId, 5)
                Log.d(TAG, "Calendar event created: id=$eventId at ${Date(timeInMillis)}")
            } else {
                Log.w(TAG, "Failed to create calendar event - no ID returned")
            }

            return eventId
        } catch (e: SecurityException) {
            Log.e(TAG, "Calendar permission denied when creating event", e)
            return null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid calendar data", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create calendar event", e)
            return null
        }
    }

    /**
     * 检查是否有日历权限
     */
    private fun hasCalendarPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除日历事件
     */
    fun deleteCalendarEvent(eventId: Long): Boolean {
        try {
            val deleteUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                eventId
            )

            val rows = context.contentResolver.delete(deleteUri, null, null)
            Log.d(TAG, "Calendar event deleted: id=$eventId, rows=$rows")
            return rows > 0
        } catch (e: SecurityException) {
            Log.e(TAG, "No calendar permission", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete calendar event", e)
            return false
        }
    }

    /**
     * 更新日历事件时间
     */
    fun updateCalendarEvent(eventId: Long, newTimeInMillis: Long): Boolean {
        try {
            val updateUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                eventId
            )

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, newTimeInMillis)
                put(CalendarContract.Events.DTEND, newTimeInMillis + 30 * 60 * 1000)
            }

            val rows = context.contentResolver.update(updateUri, values, null, null)
            Log.d(TAG, "Calendar event updated: id=$eventId, rows=$rows")
            return rows > 0
        } catch (e: SecurityException) {
            Log.e(TAG, "No calendar permission", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update calendar event", e)
            return false
        }
    }

    // ==================== 闹钟提醒 ====================

    /**
     * 设置闹钟提醒
     * @param timeInMillis 闹钟时间戳
     * @return 请求码
     */
    fun scheduleAlarm(timeInMillis: Long): Int {
        // 闹钟提醒本质上也是使用 AlarmManager，但优先级更高
        return scheduleNotificationReminder(timeInMillis)
    }

    /**
     * 取消闹钟提醒
     */
    fun cancelAlarm(requestCode: Int) {
        cancelNotificationReminder(requestCode)
    }

    // ==================== 工具方法 ====================

    /**
     * 调度精确闹钟
     */
    private fun scheduleExactAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+: 使用 setExactAndAllowWhileIdle 确保精确触发
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm set with setExactAndAllowWhileIdle")
            } else {
                // Android 6.0 以下: 使用 setExact
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm set with setExact")
            }

            // 注意：setExact 和 setExactAndAllowWhileIdle 不会自动重复
            // 需要在 BroadcastReceiver 中重新调度下一天的闹钟
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for exact alarm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set exact alarm", e)
        }
    }

    /**
     * 调度不精确闹钟（作为降级方案）
     */
    private fun scheduleInexactAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        try {
            // 使用 setWindow 设置一个时间窗口（允许系统在1小时内触发）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    60 * 60 * 1000, // 1小时窗口
                    pendingIntent
                )
                Log.d(TAG, "Inexact alarm set with setWindow (1 hour window)")
            } else {
                // Android 6.0 以下使用 set（本身就是不精确的）
                alarmManager?.set(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Inexact alarm set with set")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set inexact alarm", e)
        }
    }

    /**
     * 检查是否可以调度精确闹钟
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    /**
     * 获取默认日历ID
     */
    private fun getDefaultCalendarId(): Long? {
        try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.IS_PRIMARY
            )

            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                    return it.getLong(idIndex)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No calendar permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get default calendar", e)
        }

        return null
    }

    /**
     * 为日历事件添加提醒
     */
    private fun addEventReminder(eventId: Long, minutesBefore: Int) {
        try {
            val values = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }

            context.contentResolver.insert(
                CalendarContract.Reminders.CONTENT_URI,
                values
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add event reminder", e)
        }
    }

    /**
     * 生成唯一的请求码
     */
    private fun generateRequestCode(): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCode = prefs.getInt(KEY_LAST_REQUEST_CODE, 0)
        val newCode = if (lastCode >= Int.MAX_VALUE - 1000) {
            Random.nextInt(1000, 10000)
        } else {
            lastCode + 1
        }
        prefs.edit().putInt(KEY_LAST_REQUEST_CODE, newCode).apply()
        return newCode
    }

    /**
     * 计算下次提醒时间
     * 如果指定时间已过，则设为明天同一时间
     */
    fun calculateNextReminderTime(hourOfDay: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果今天的这个时间已经过了，就设为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return calendar.timeInMillis
    }
}

