package com.example.english.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,  // 单例设置
    val fontSize: Float = 16f,         // 字体大小（sp）
    val backgroundColor: Long = 0xFFFFFFFF,  // 背景颜色（ARGB）
    val primaryColor: Long = 0xFF6200EE,     // 主题色
    val themeMode: String = "LIGHT",    // LIGHT, DARK
    val isAutoBackup: Boolean = false,  // 自动备份（预留）
    val backupPath: String = "",        // 备份路径（预留）

    // 学习提醒功能
    val studyReminder: Boolean = false,        // 是否启用学习提醒
    val reminderTime: String = "09:00",        // 提醒时间（HH:mm格式，保留用于显示）
    val reminderTimeMillis: Long = 0L,         // 提醒时间戳（用于调度）
    val reminderType: String = "NOTIFICATION", // 提醒方式：NOTIFICATION, CALENDAR, ALARM
    val calendarEventId: Long? = null,         // 日历事件ID（日历方式使用）
    val alarmRequestCode: Int? = null          // 闹钟请求码（闹钟方式使用）
)

enum class ThemeMode {
    LIGHT,
    DARK
}

enum class ReminderType {
    NOTIFICATION,  // 通知提醒
    CALENDAR,      // 日历事件
    ALARM          // 闹钟提醒
}

