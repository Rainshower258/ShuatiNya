package com.example.english.data.repository

import com.example.english.data.local.dao.SettingsDao
import com.example.english.data.local.entity.SettingsEntity
import com.example.english.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao,
    private val preferences: SettingsPreferences
) {
    val settings: Flow<SettingsEntity> = settingsDao.getSettings()
        .map { it ?: SettingsEntity() }

    val fontSize: Flow<Float> = preferences.fontSizeFlow
    val backgroundColor: Flow<Long> = preferences.backgroundColorFlow
    val primaryColor: Flow<Long> = preferences.primaryColorFlow
    val themeMode: Flow<String> = preferences.themeModeFlow

    // 学习提醒相关 Flow
    val studyReminderEnabled: Flow<Boolean> = preferences.studyReminderEnabledFlow
    val reminderTimeMillis: Flow<Long> = preferences.reminderTimeMillisFlow
    val reminderType: Flow<String> = preferences.reminderTypeFlow

    suspend fun updateFontSize(size: Float) {
        preferences.updateFontSize(size)
        // 确保数据库存在记录
        ensureSettingsExists()
        settingsDao.updateFontSize(size)
    }

    suspend fun updateBackgroundColor(color: Long) {
        preferences.updateBackgroundColor(color)
        ensureSettingsExists()
        settingsDao.updateBackgroundColor(color)
    }

    suspend fun updateThemeMode(mode: String) {
        preferences.updateThemeMode(mode)
        ensureSettingsExists()
        settingsDao.updateThemeMode(mode)
    }

    suspend fun updateStudyReminder(enabled: Boolean) {
        preferences.updateStudyReminderEnabled(enabled)
        ensureSettingsExists()
        settingsDao.updateStudyReminder(enabled)
    }

    suspend fun updateAutoBackup(enabled: Boolean) {
        ensureSettingsExists()
        settingsDao.updateAutoBackup(enabled)
    }

    suspend fun updateBackupPath(path: String) {
        ensureSettingsExists()
        settingsDao.updateBackupPath(path)
    }

    // 学习提醒相关更新方法
    suspend fun updateReminderTimeMillis(timeMillis: Long) {
        preferences.updateReminderTimeMillis(timeMillis)
        ensureSettingsExists()
        settingsDao.updateReminderTime(timeMillis)
    }

    suspend fun updateReminderType(type: String) {
        preferences.updateReminderType(type)
        ensureSettingsExists()
        settingsDao.updateReminderType(type)
    }

    suspend fun updateCalendarEventId(eventId: Long?) {
        preferences.updateCalendarEventId(eventId)
        ensureSettingsExists()
        settingsDao.updateCalendarEventId(eventId)
    }

    suspend fun updateAlarmRequestCode(requestCode: Int?) {
        preferences.updateAlarmRequestCode(requestCode)
        ensureSettingsExists()
        settingsDao.updateAlarmRequestCode(requestCode)
    }

    suspend fun getCalendarEventId(): Long? {
        return settingsDao.getCalendarEventId()
    }

    suspend fun getAlarmRequestCode(): Int? {
        return settingsDao.getAlarmRequestCode()
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.saveSettings(settings)
    }

    private suspend fun ensureSettingsExists() {
        // 静默插入，如果已存在则不会覆盖（因为使用 REPLACE 策略）
        try {
            settingsDao.saveSettings(SettingsEntity())
        } catch (e: Exception) {
            // 忽略错误，可能已经存在
        }
    }
}

