package com.example.english.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val BG_COLOR = longPreferencesKey("background_color")
        val PRIMARY_COLOR = longPreferencesKey("primary_color")
        val THEME_MODE = stringPreferencesKey("theme_mode")

        // 学习提醒相关
        val STUDY_REMINDER_ENABLED = booleanPreferencesKey("study_reminder_enabled")
        val REMINDER_TIME_MILLIS = longPreferencesKey("reminder_time_millis")
        val REMINDER_TYPE = stringPreferencesKey("reminder_type")
        val CALENDAR_EVENT_ID = longPreferencesKey("calendar_event_id")
        val ALARM_REQUEST_CODE = intPreferencesKey("alarm_request_code")
    }

    val fontSizeFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_SIZE] ?: 16f
        }

    val backgroundColorFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[BG_COLOR] ?: 0xFFFFFFFF
        }

    val primaryColorFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PRIMARY_COLOR] ?: 0xFF6200EE
        }

    val themeModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "LIGHT"
        }

    // 学习提醒相关 Flow
    val studyReminderEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[STUDY_REMINDER_ENABLED] ?: false
        }

    val reminderTimeMillisFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[REMINDER_TIME_MILLIS] ?: 0L
        }

    val reminderTypeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[REMINDER_TYPE] ?: "NOTIFICATION"
        }

    suspend fun updateFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun updateBackgroundColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[BG_COLOR] = color
        }
    }

    suspend fun updatePrimaryColor(color: Long) {
        context.dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR] = color
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    // 学习提醒相关更新方法
    suspend fun updateStudyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STUDY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateReminderTimeMillis(timeMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_TIME_MILLIS] = timeMillis
        }
    }

    suspend fun updateReminderType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_TYPE] = type
        }
    }

    suspend fun updateCalendarEventId(eventId: Long?) {
        context.dataStore.edit { preferences ->
            if (eventId != null) {
                preferences[CALENDAR_EVENT_ID] = eventId
            } else {
                preferences.remove(CALENDAR_EVENT_ID)
            }
        }
    }

    suspend fun updateAlarmRequestCode(requestCode: Int?) {
        context.dataStore.edit { preferences ->
            if (requestCode != null) {
                preferences[ALARM_REQUEST_CODE] = requestCode
            } else {
                preferences.remove(ALARM_REQUEST_CODE)
            }
        }
    }
}
