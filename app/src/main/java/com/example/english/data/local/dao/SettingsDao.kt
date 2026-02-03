package com.example.english.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.english.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Query("UPDATE settings SET fontSize = :fontSize WHERE id = 1")
    suspend fun updateFontSize(fontSize: Float)

    @Query("UPDATE settings SET backgroundColor = :color WHERE id = 1")
    suspend fun updateBackgroundColor(color: Long)

    @Query("UPDATE settings SET themeMode = :themeMode WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String)

    @Query("UPDATE settings SET studyReminder = :enabled WHERE id = 1")
    suspend fun updateStudyReminder(enabled: Boolean)

    @Query("UPDATE settings SET isAutoBackup = :enabled WHERE id = 1")
    suspend fun updateAutoBackup(enabled: Boolean)

    @Query("UPDATE settings SET backupPath = :path WHERE id = 1")
    suspend fun updateBackupPath(path: String)

    // 学习提醒相关操作
    @Query("UPDATE settings SET reminderTimeMillis = :timeMillis WHERE id = 1")
    suspend fun updateReminderTime(timeMillis: Long)

    @Query("UPDATE settings SET reminderType = :type WHERE id = 1")
    suspend fun updateReminderType(type: String)

    @Query("UPDATE settings SET calendarEventId = :eventId WHERE id = 1")
    suspend fun updateCalendarEventId(eventId: Long?)

    @Query("UPDATE settings SET alarmRequestCode = :requestCode WHERE id = 1")
    suspend fun updateAlarmRequestCode(requestCode: Int?)

    @Query("SELECT calendarEventId FROM settings WHERE id = 1")
    suspend fun getCalendarEventId(): Long?

    @Query("SELECT alarmRequestCode FROM settings WHERE id = 1")
    suspend fun getAlarmRequestCode(): Int?
}

