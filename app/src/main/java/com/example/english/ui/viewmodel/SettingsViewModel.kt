package com.example.english.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.english.EnglishApp
import com.example.english.data.database.AppDatabase
import com.example.english.data.local.entity.SettingsEntity
import com.example.english.data.preferences.SettingsPreferences
import com.example.english.data.repository.SettingsRepository
import com.example.english.util.AppLogger
import com.example.english.util.PermissionManager
import com.example.english.util.ReminderManager
import com.example.english.util.DatabaseOperationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Job
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: SettingsRepository
    private val reminderManager = ReminderManager(context)
    private val permissionManager = PermissionManager(context)

    // 添加权限状态
    private val _needsStoragePermission = MutableStateFlow(false)
    val needsStoragePermission = _needsStoragePermission.asStateFlow()

    // 应用锁定状态（恢复备份期间锁定整个应用）
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    private val _lockReason = MutableStateFlow("")
    val lockReason = _lockReason.asStateFlow()

    // 详细恢复进度
    data class RestoreProgress(
        val currentTable: String = "",
        val currentCount: Int = 0,
        val totalCount: Int = 0,
        val percentage: Int = 0
    )

    private val _restoreProgress = MutableStateFlow(RestoreProgress())
    val restoreProgress = _restoreProgress.asStateFlow()

    // 是否需要重启应用
    private val _needsAppRestart = MutableStateFlow(false)
    val needsAppRestart = _needsAppRestart.asStateFlow()

    // 备份/恢复操作状态
    enum class BackupOperationState {
        IDLE,              // 空闲
        CHECKING,          // 检查中
        PREPARING,         // 准备中
        BACKING_UP,        // 备份中
        RESTORING,         // 恢复中
        VALIDATING,        // 验证中
        COMPLETED,         // 完成
        FAILED             // 失败
    }

    private val _operationState = MutableStateFlow(BackupOperationState.IDLE)
    val operationState = _operationState.asStateFlow()

    private val _operationProgress = MutableStateFlow("")
    val operationProgress = _operationProgress.asStateFlow()

    // 🥚 Easter Egg: 彩蛋状态管理
    enum class EasterEggType {
        CLICK,      // 连续点击6次
        LONGPRESS   // 长按1.5秒
    }

    private val _easterEggClickCount = MutableStateFlow(0)
    val easterEggClickCount = _easterEggClickCount.asStateFlow()

    private val _lastEasterEggClickTime = MutableStateFlow(0L)

    private val _showEasterEggDialog = MutableStateFlow(false)
    val showEasterEggDialog = _showEasterEggDialog.asStateFlow()

    private val _easterEggType = MutableStateFlow<EasterEggType?>(null)
    val easterEggType = _easterEggType.asStateFlow()

    init {
        // ✅ 修复 InvalidationTracker 双重初始化问题
        // 从 Application 获取已初始化的数据库实例，避免重复调用 getDatabase()
        val database = (application as EnglishApp).database
        val preferences = SettingsPreferences(application)
        repository = SettingsRepository(database.settingsDao(), preferences)

        // 异步初始化默认设置
        viewModelScope.launch {
            try {
                repository.saveSettings(SettingsEntity())
            } catch (e: Exception) {
                // 忽略，可能已存在
            }
        }
    }

    val settings: StateFlow<SettingsEntity> = repository.settings
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsEntity()
        )

    val fontSize: StateFlow<Float> = repository.fontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 16f)

    val backgroundColor: StateFlow<Long> = repository.backgroundColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFFFFFFF)

    val primaryColor: StateFlow<Long> = repository.primaryColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFF6200EE)

    val themeMode: StateFlow<String> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "LIGHT")

    // 学习提醒相关状态
    val studyReminderEnabled: StateFlow<Boolean> = repository.studyReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderTimeMillis: StateFlow<Long> = repository.reminderTimeMillis
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val reminderType: StateFlow<String> = repository.reminderType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "NOTIFICATION")

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            repository.updateFontSize(size)
        }
    }

    fun updateBackgroundColor(color: Long) {
        viewModelScope.launch {
            repository.updateBackgroundColor(color)
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateStudyReminder(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateStudyReminder(enabled)

            if (enabled) {
                // 开启提醒时，如果已设置时间则调度
                val timeMillis = reminderTimeMillis.value
                if (timeMillis > 0) {
                    scheduleReminder(timeMillis)
                }
            } else {
                // 关闭提醒时，取消所有提醒
                cancelReminder()
            }
        }
    }

    // ❌ 已弃用：自动备份功能已移除
    // fun updateAutoBackup(enabled: Boolean) {
    //     viewModelScope.launch {
    //         repository.updateAutoBackup(enabled)
    //     }
    // }

    // 更新提醒时间
    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val timeMillis = reminderManager.calculateNextReminderTime(hour, minute)
            repository.updateReminderTimeMillis(timeMillis)

            // 如果提醒已开启，重新调度
            if (studyReminderEnabled.value) {
                scheduleReminder(timeMillis)
            }
        }
    }

    // 更新提醒方式
    fun updateReminderType(type: String) {
        viewModelScope.launch {
            // 先取消旧的提醒
            cancelReminder()

            // 更新类型
            repository.updateReminderType(type)

            // 如果提醒已开启，创建新的提醒
            if (studyReminderEnabled.value) {
                val timeMillis = reminderTimeMillis.value
                if (timeMillis > 0) {
                    scheduleReminder(timeMillis)
                }
            }
        }
    }

    // 调度提醒
    private suspend fun scheduleReminder(timeMillis: Long) {
        val type = reminderType.value

        when (type) {
            "NOTIFICATION" -> {
                // 检查通知权限
                if (!permissionManager.checkNotificationPermission()) {
                    AppLogger.w("No notification permission, cannot schedule notification reminder")
                    return
                }

                val requestCode = reminderManager.scheduleNotificationReminder(timeMillis)
                if (requestCode > 0) {
                    repository.updateAlarmRequestCode(requestCode)
                    AppLogger.d("Notification reminder scheduled successfully")
                } else {
                    AppLogger.e("Failed to schedule notification reminder")
                }
            }
            "CALENDAR" -> {
                // 检查日历权限
                if (!permissionManager.checkCalendarPermission()) {
                    AppLogger.w("No calendar permission, cannot create calendar event")
                    return
                }

                val eventId = reminderManager.createCalendarEvent(timeMillis)
                if (eventId != null) {
                    repository.updateCalendarEventId(eventId)
                    AppLogger.d("Calendar event created successfully: id=$eventId")
                } else {
                    AppLogger.e("Failed to create calendar event")
                }
            }
            "ALARM" -> {
                // 检查闹钟权限
                if (!permissionManager.checkAlarmPermission()) {
                    AppLogger.w("No exact alarm permission, will use inexact alarm")
                    // 仍然尝试调度，会自动降级为不精确闹钟
                }

                val requestCode = reminderManager.scheduleAlarm(timeMillis)
                if (requestCode > 0) {
                    repository.updateAlarmRequestCode(requestCode)
                    AppLogger.d("Alarm scheduled successfully")
                } else {
                    AppLogger.e("Failed to schedule alarm")
                }
            }
        }
    }

    // 取消提醒
    private suspend fun cancelReminder() {
        val type = reminderType.value

        when (type) {
            "NOTIFICATION", "ALARM" -> {
                val requestCode = repository.getAlarmRequestCode()
                requestCode?.let {
                    if (type == "NOTIFICATION") {
                        reminderManager.cancelNotificationReminder(it)
                    } else {
                        reminderManager.cancelAlarm(it)
                    }
                }
            }
            "CALENDAR" -> {
                val eventId = repository.getCalendarEventId()
                eventId?.let {
                    reminderManager.deleteCalendarEvent(it)
                }
            }
        }
    }

    // 检查权限
    fun checkPermissions(): List<String> {
        return permissionManager.getMissingPermissions()
    }


    // 测试提醒功能（立即触发一次）
    fun testReminder() {
        viewModelScope.launch {
            val type = reminderType.value
            AppLogger.d("Testing reminder type: $type")

            when (type) {
                "NOTIFICATION" -> {
                    if (!permissionManager.checkNotificationPermission()) {
                        AppLogger.w("No notification permission for test")
                        return@launch
                    }
                    // 直接显示通知
                    com.example.english.util.NotificationHelper.showStudyNotification(context)
                    AppLogger.d("Test notification sent")
                }
                "CALENDAR" -> {
                    if (!permissionManager.checkCalendarPermission()) {
                        AppLogger.w("No calendar permission for test")
                        return@launch
                    }
                    // 创建一个测试事件（1小时后）
                    val testTime = System.currentTimeMillis() + 60 * 60 * 1000
                    val eventId = reminderManager.createCalendarEvent(testTime)
                    AppLogger.d("Test calendar event created: $eventId")
                }
                "ALARM" -> {
                    if (!permissionManager.checkAlarmPermission()) {
                        AppLogger.w("No exact alarm permission for test")
                    }
                    // 设置一个1分钟后的测试闹钟
                    val testTime = System.currentTimeMillis() + 60 * 1000
                    val requestCode = reminderManager.scheduleNotificationReminder(testTime)
                    AppLogger.d("Test alarm scheduled for 1 minute later: requestCode=$requestCode")
                }
            }
        }
    }

    // 获取提醒设置状态信息
    suspend fun getReminderStatus(): String {
        val type = reminderType.value
        val timeMillis = reminderTimeMillis.value

        return when (type) {
            "NOTIFICATION", "ALARM" -> {
                val requestCode = repository.getAlarmRequestCode()
                if (requestCode != null && requestCode > 0) {
                    "✅ 已设置${if (type == "NOTIFICATION") "通知" else "闹钟"}提醒 (ID: $requestCode)"
                } else {
                    "❌ 未设置或设置失败"
                }
            }
            "CALENDAR" -> {
                val eventId = repository.getCalendarEventId()
                if (eventId != null && eventId > 0) {
                    "✅ 已创建日历事件 (ID: $eventId)"
                } else {
                    "❌ 未创建或创建失败"
                }
            }
            else -> "⚠️ 未知提醒类型"
        }
    }

    // 检查提醒是否真正设置成功
    fun verifyReminderSetup(): Boolean {
        val type = reminderType.value

        return when (type) {
            "NOTIFICATION" -> {
                permissionManager.checkNotificationPermission() &&
                reminderManager.canScheduleExactAlarms()
            }
            "CALENDAR" -> {
                permissionManager.checkCalendarPermission()
            }
            "ALARM" -> {
                reminderManager.canScheduleExactAlarms()
            }
            else -> false
        }
    }

    // ========== 本地备份功能 ==========

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _currentBackupPath = MutableStateFlow<String>("")
    val currentBackupPath: StateFlow<String> = _currentBackupPath.asStateFlow()

    // ✅ 操作锁：防止并发操作
    private var currentOperationJob: Job? = null

    // ✅ 自动清除Job：用于取消过期的状态清除
    private var autoClearJob: Job? = null

    // ✅ 防抖：记录最后操作时间
    private var lastOperationTime = 0L
    private val OPERATION_DEBOUNCE_MS = 300L

    // ✅ 操作冷却时间：防止操作完成后立即进行新操作
    private var lastOperationCompleteTime = 0L
    private val OPERATION_COOLDOWN_MS = 1000L

    init {
        // 初始化备份路径
        _currentBackupPath.value = getDefaultBackupPath()
    }

    /**
     * 获取默认备份路径
     */
    private fun getDefaultBackupPath(): String {
        val backupDir = File(getApplication<Application>().getExternalFilesDir(null), "backups")
        return backupDir.absolutePath
    }

    /**
     * 获取备份目录
     */
    private fun getBackupDirectory(): File {
        // 从 settings 中读取自定义路径，如果没有则使用默认路径
        val customPath = settings.value.backupPath
        return if (customPath.isNotEmpty() && File(customPath).exists()) {
            File(customPath)
        } else {
            File(getApplication<Application>().getExternalFilesDir(null), "backups")
        }
    }

    // ❌ 已弃用：自动备份功能已移除
    // /**
    //  * 更新备份路径
    //  */
    // fun updateBackupPath(path: String) {
    //     viewModelScope.launch {
    //         try {
    //             val newDir = File(path)
    //             if (!newDir.exists()) {
    //                 newDir.mkdirs()
    //             }
    //             repository.updateBackupPath(path)
    //             _currentBackupPath.value = path
    //             _backupMessage.value = "✅ 备份路径已更新"
    //         } catch (e: Exception) {
    //             _backupMessage.value = "❌ 路径设置失败：${e.message}"
    //             AppLogger.e("Update backup path failed", e)
    //         }
    //     }
    // }

    // ❌ 已弃用：自动备份功能已移除
    // /**
    //  * 重置为默认备份路径
    //  */
    // fun resetBackupPath() {
    //     viewModelScope.launch {
    //         val defaultPath = getDefaultBackupPath()
    //         repository.updateBackupPath("")
    //         _currentBackupPath.value = defaultPath
    //         _backupMessage.value = "✅ 已重置为默认路径"
    //     }
    // }

    /**
     * 获取备份路径信息（用于显示）
     */
    fun getBackupPathInfo(): String {
        val backupDir = getBackupDirectory()
        val fileCount = backupDir.listFiles()?.count { it.extension == "db" } ?: 0
        return "路径: ${backupDir.absolutePath}\n备份数量: $fileCount"
    }

    /**
     * 创建手动备份
     */
    fun createManualBackup() {
        // ✅ 防抖检查：300ms内的重复点击直接忽略
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOperationTime < OPERATION_DEBOUNCE_MS) {
            AppLogger.w("Operation debounced: too frequent clicks")
            return
        }

        // ✅ 冷却时间检查：操作完成后1秒内不允许新操作
        if (currentTime - lastOperationCompleteTime < OPERATION_COOLDOWN_MS) {
            _backupMessage.value = "操作过于频繁，请稍后再试"
            AppLogger.w("Operation in cooldown period")
            return
        }

        lastOperationTime = currentTime

        // ✅ 如果有操作正在进行，直接返回
        if (_isBackingUp.value) {
            AppLogger.w("Operation already in progress, ignoring request")
            return
        }

        // ✅ 取消之前的操作和自动清除
        currentOperationJob?.cancel()
        autoClearJob?.cancel()

        currentOperationJob = viewModelScope.launch {

            // ✅ 立即重置状态，防止显示上次操作的结果
            _operationState.value = BackupOperationState.IDLE
            _operationProgress.value = ""
            _backupMessage.value = null
            _restoreProgress.value = RestoreProgress()

            _isBackingUp.value = true
            _operationState.value = BackupOperationState.CHECKING
            _backupMessage.value = null

            try {
                // 1. 检查存储权限
                _operationProgress.value = "正在检查存储权限..."
                delay(200) // 短暂延迟让用户看到进度

                if (!permissionManager.checkStoragePermission()) {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 备份失败：缺少存储权限\n请在应用设置中授予存储权限"
                    _needsStoragePermission.value = true
                    _isBackingUp.value = false
                    return@launch
                }

                // 2. 准备备份
                _operationState.value = BackupOperationState.PREPARING
                _operationProgress.value = "正在准备备份..."

                val dbFile = getApplication<Application>().getDatabasePath("english_learning_sun6_db")

                if (!dbFile.exists()) {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 备份失败：数据库文件不存在\n路径: ${dbFile.absolutePath}"
                    AppLogger.e("Backup failed: database file not found at ${dbFile.absolutePath}")
                    _isBackingUp.value = false
                    return@launch
                }

                // 3. 执行 WAL Checkpoint
                _operationProgress.value = "正在同步数据库..."
                try {
                    // ✅ 使用已存在的数据库实例，避免重复初始化
                    val database = (getApplication<Application>() as EnglishApp).database
                    database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                        if (cursor.moveToFirst()) {
                            val busy = cursor.getInt(0)
                            val log = cursor.getInt(1)
                            val checkpointed = cursor.getInt(2)
                            AppLogger.i("WAL checkpoint executed successfully: busy=$busy, log=$log, checkpointed=$checkpointed")
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e("WAL checkpoint failed, backup may be incomplete", e)
                }

                // 4. 创建备份文件
                _operationState.value = BackupOperationState.BACKING_UP
                _operationProgress.value = "正在备份数据..."

                val backupDir = getBackupDirectory()
                if (!backupDir.exists()) backupDir.mkdirs()

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val backupFileName = "backup_${sdf.format(Date())}.db"
                val backupFile = File(backupDir, backupFileName)

                // 复制数据库文件
                dbFile.copyTo(backupFile, overwrite = false)

                // 5. 验证备份完整性
                _operationState.value = BackupOperationState.VALIDATING
                _operationProgress.value = "正在验证备份完整性..."
                delay(300)

                if (!validateDatabaseFile(backupFile)) {
                    backupFile.delete()
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 备份失败：生成的备份文件损坏\n" +
                            "可能原因：数据库正在被其他进程使用\n" +
                            "建议：稍后重试"
                    AppLogger.e("Backup validation failed, file deleted: $backupFileName")
                    _isBackingUp.value = false
                    return@launch
                }

                // 6. 备份完成
                _operationState.value = BackupOperationState.COMPLETED
                _operationProgress.value = "备份完成！"
                _backupMessage.value = "✅ 备份成功：$backupFileName"
                AppLogger.i("Backup created and validated: $backupFileName at ${backupDir.absolutePath}")

                // ✅ 记录操作完成时间，启动冷却计时
                lastOperationCompleteTime = System.currentTimeMillis()

                // ✅ 使用可取消的Job实现自动清除
                autoClearJob?.cancel()
                autoClearJob = viewModelScope.launch {
                    delay(3000)
                    // 只有在状态仍为COMPLETED且没有新操作时才清除
                    if (_operationState.value == BackupOperationState.COMPLETED && !_isBackingUp.value) {
                        _operationState.value = BackupOperationState.IDLE
                        _operationProgress.value = ""
                        AppLogger.i("Auto-cleared backup completion state")
                    }
                }

            } catch (e: Exception) {
                _operationState.value = BackupOperationState.FAILED

                // 判断是否为权限问题
                val errorMessage = when {
                    e.message?.contains("Permission denied", ignoreCase = true) == true ||
                    e.message?.contains("EACCES", ignoreCase = true) == true -> {
                        _needsStoragePermission.value = true
                        "❌ 备份失败：权限不足\n请在应用设置中授予存储权限"
                    }
                    e.message?.contains("No such file", ignoreCase = true) == true -> {
                        "❌ 备份失败：数据库文件不存在\n请先使用应用创建数据"
                    }
                    else -> "❌ 备份失败：${e.message}"
                }
                _backupMessage.value = errorMessage
                _operationProgress.value = ""
                AppLogger.e("Backup failed", e)
            } finally {
                // ✅ 立即清除操作标志（不要等待）
                _isBackingUp.value = false
            }
        }
    }

    /**
     * 验证数据库文件完整性
     */
    private fun validateDatabaseFile(dbFile: File): Boolean {
        var database: android.database.sqlite.SQLiteDatabase? = null
        try {
            // 尝试以只读模式打开数据库
            database = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )

            // 执行完整性检查
            val cursor = database.rawQuery("PRAGMA integrity_check", null)
            var isValid = false

            if (cursor.moveToFirst()) {
                val result = cursor.getString(0)
                isValid = result.equals("ok", ignoreCase = true)
                AppLogger.i("Database integrity check result: $result")
            }

            cursor.close()
            return isValid
        } catch (e: Exception) {
            AppLogger.e("Database validation failed", e)
            return false
        } finally {
            database?.close()
        }
    }

    /**
     * 恢复备份 - 使用DatabaseOperationManager统一管理
     */
    fun restoreBackup(backupFileName: String) {
        // ✅ 防抖检查：300ms内的重复点击直接忽略
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOperationTime < OPERATION_DEBOUNCE_MS) {
            AppLogger.w("Restore operation debounced: too frequent clicks")
            return
        }

        // ✅ 冷却时间检查：操作完成后1秒内不允许新操作
        if (currentTime - lastOperationCompleteTime < OPERATION_COOLDOWN_MS) {
            _backupMessage.value = "操作过于频繁，请稍后再试"
            AppLogger.w("Restore operation in cooldown period")
            return
        }

        lastOperationTime = currentTime

        // ✅ 如果有操作正在进行，直接返回
        if (_isBackingUp.value) {
            AppLogger.w("Restore operation already in progress, ignoring request")
            return
        }

        // ✅ 取消之前的操作和自动清除
        currentOperationJob?.cancel()
        autoClearJob?.cancel()

        currentOperationJob = viewModelScope.launch {
            // 立即重置所有状态
            _operationState.value = BackupOperationState.IDLE
            _operationProgress.value = ""
            _backupMessage.value = null
            _restoreProgress.value = RestoreProgress()
            _needsAppRestart.value = false
            _isBackingUp.value = true

            try {
                // 检查存储权限
                if (!permissionManager.checkStoragePermission()) {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 恢复失败：缺少存储权限\n请在应用设置中授予存储权限"
                    _needsStoragePermission.value = true
                    _isBackingUp.value = false
                    return@launch
                }

                val backupDir = getBackupDirectory()
                val backupFile = File(backupDir, backupFileName)

                if (!backupFile.exists()) {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 备份文件不存在\n路径: ${backupFile.absolutePath}"
                    _isBackingUp.value = false
                    return@launch
                }

                // 修复文件权限（如需要）
                if (!backupFile.canRead()) {
                    AppLogger.w("Backup file is not readable, attempting to fix permissions")
                    try {
                        val tempFile = File(backupDir, "${backupFileName}.temp")
                        backupFile.inputStream().use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        backupFile.delete()
                        tempFile.renameTo(backupFile)
                    } catch (fixError: Exception) {
                        _operationState.value = BackupOperationState.FAILED
                        _backupMessage.value = "❌ 恢复失败：文件权限错误"
                        _isBackingUp.value = false
                        return@launch
                    }
                }

                // 验证数据库完整性
                if (!validateDatabaseFile(backupFile)) {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 恢复失败：数据库文件已损坏"
                    _isBackingUp.value = false
                    return@launch
                }

                // 检查版本兼容性
                val backupVersion = getBackupDatabaseVersion(backupFile)
                val currentVersion = 6
                if (backupVersion > currentVersion) {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 恢复失败：版本不兼容\n备份版本: $backupVersion\n当前版本: $currentVersion"
                    _isBackingUp.value = false
                    return@launch
                }

                // 使用DatabaseOperationManager执行恢复
                val result = DatabaseOperationManager.executeCriticalOperation(
                    context = getApplication<Application>().applicationContext,
                    operationType = DatabaseOperationManager.OperationType.RESTORING_BACKUP,
                    description = "恢复备份「$backupFileName」"
                ) { updateProgress, updateDetailedProgress ->
                    updateProgress("正在准备恢复...", 5)

                    withContext(Dispatchers.IO) {
                        // 打开备份数据库
                        val backupDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                            backupFile.absolutePath,
                            null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                        )

                        updateProgress("正在打开数据库...", 10)

                        val currentDb = (getApplication<Application>() as EnglishApp)
                            .database.openHelper.writableDatabase

                        try {
                            currentDb.beginTransaction()
                            try {
                                // 迁移各个表
                                updateProgress("正在迁移词库数据...", 20)
                                migrateTableWithProgress(backupDb, currentDb, "decks", "词库", updateDetailedProgress)

                                updateProgress("正在迁移单词数据...", 40)
                                migrateTableWithProgress(backupDb, currentDb, "words", "单词", updateDetailedProgress)

                                updateProgress("正在迁移题目数据...", 60)
                                migrateTableWithProgress(backupDb, currentDb, "questions", "题目", updateDetailedProgress)

                                updateProgress("正在迁移学习记录...", 75)
                                migrateTableWithProgress(backupDb, currentDb, "study_sessions", "学习记录", updateDetailedProgress)

                                updateProgress("正在迁移设置...", 85)
                                migrateTableWithProgress(backupDb, currentDb, "settings", "设置", updateDetailedProgress)

                                currentDb.setTransactionSuccessful()
                                updateProgress("正在同步数据库...", 90)
                            } finally {
                                currentDb.endTransaction()

                                // 强制数据库同步
                                currentDb.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                                    if (cursor.moveToFirst()) {
                                        AppLogger.i("WAL checkpoint: busy=${cursor.getInt(0)}, log=${cursor.getInt(1)}")
                                    }
                                }
                                currentDb.query("PRAGMA shrink_memory").close()
                                delay(300)
                            }
                        } finally {
                            backupDb.close()
                        }

                        updateProgress("恢复完成！建议重启应用", 100)
                    }
                }

                if (result.isSuccess) {
                    _operationState.value = BackupOperationState.COMPLETED
                    _backupMessage.value = "✅ 恢复成功！建议重启应用"
                    _needsAppRestart.value = true
                    lastOperationCompleteTime = System.currentTimeMillis()
                } else {
                    _operationState.value = BackupOperationState.FAILED
                    _backupMessage.value = "❌ 恢复失败：${result.exceptionOrNull()?.message}"
                }

            } catch (e: Exception) {
                _operationState.value = BackupOperationState.FAILED
                _backupMessage.value = "❌ 恢复失败：${e.message}"
                AppLogger.e("Restore failed", e)
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    /**
     * 迁移表并更新详细进度
     */
    private suspend fun migrateTableWithProgress(
        backupDb: android.database.sqlite.SQLiteDatabase,
        currentDb: SupportSQLiteDatabase,
        tableName: String,
        tableDisplayName: String,
        updateDetailedProgress: (DatabaseOperationManager.RestoreProgress) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            currentDb.execSQL("DELETE FROM $tableName")
            AppLogger.i("Cleared table: $tableName")

            val countCursor = backupDb.rawQuery("SELECT COUNT(*) FROM $tableName", null)
            val totalCount = if (countCursor.moveToFirst()) countCursor.getInt(0) else 0
            countCursor.close()

            if (totalCount == 0) {
                AppLogger.i("Table $tableName is empty, skipping migration")
                updateDetailedProgress(DatabaseOperationManager.RestoreProgress(tableDisplayName, 0, 0, 0))
                return@withContext
            }

            val cursor = backupDb.rawQuery("SELECT * FROM $tableName", null)
            val columnNames = cursor.columnNames
            var migratedCount = 0
            val batchSize = 100

            try {
                while (cursor.moveToNext()) {
                    val values = mutableListOf<String>()
                    for (i in columnNames.indices) {
                        val value = when (cursor.getType(i)) {
                            android.database.Cursor.FIELD_TYPE_NULL -> "NULL"
                            android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i).toString()
                            android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i).toString()
                            android.database.Cursor.FIELD_TYPE_STRING -> "'${cursor.getString(i).replace("'", "''")}'"
                            android.database.Cursor.FIELD_TYPE_BLOB -> "X'${cursor.getBlob(i).joinToString("") { "%02X".format(it) }}'"
                            else -> "NULL"
                        }
                        values.add(value)
                    }

                    val sql = "INSERT INTO $tableName (${columnNames.joinToString(",")}) VALUES (${values.joinToString(",")})"
                    currentDb.execSQL(sql)
                    migratedCount++

                    if (migratedCount % batchSize == 0 || migratedCount == totalCount) {
                        val percentage = (migratedCount * 100 / totalCount)
                        updateDetailedProgress(
                            DatabaseOperationManager.RestoreProgress(
                                tableDisplayName,
                                migratedCount,
                                totalCount,
                                percentage
                            )
                        )
                        if (totalCount > 1000) delay(5)
                    }
                }
            } finally {
                cursor.close()
            }

            AppLogger.i("Migrated $migratedCount rows from table $tableName")
            delay(100)
        }
    }


    /**
     * 获取备份数据库的版本号
     */
    private fun getBackupDatabaseVersion(backupFile: File): Int {
        var database: android.database.sqlite.SQLiteDatabase? = null
        return try {
            database = android.database.sqlite.SQLiteDatabase.openDatabase(
                backupFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            val cursor = database.rawQuery("PRAGMA user_version", null)
            val version = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()
            version
        } catch (e: Exception) {
            AppLogger.e("Failed to get backup database version", e)
            0
        } finally {
            database?.close()
        }
    }

    /**
     * 重启应用
     */
    fun restartApp() {
        try {
            val context = getApplication<Application>()
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent)

            AppLogger.i("App restart scheduled")
            kotlin.system.exitProcess(0)
        } catch (e: Exception) {
            AppLogger.e("Failed to restart app", e)
        }
    }

    /**
     * 取消重启（用户选择稍后重启）
     */
    fun dismissRestartDialog() {
        _needsAppRestart.value = false
        _isAppLocked.value = false  // 🔓 解锁应用
    }

    /**
     * 获取备份文件列表
     */
    fun getBackupFiles(): List<BackupFileInfo> {
        return try {
            val backupDir = getBackupDirectory()
            if (!backupDir.exists()) return emptyList()

            backupDir.listFiles()
                ?.filter { it.extension == "db" }
                ?.map { file ->
                    // 检查每个备份文件的有效性
                    val isValid = try {
                        validateDatabaseFile(file)
                    } catch (e: Exception) {
                        AppLogger.e("Failed to validate backup: ${file.name}", e)
                        false
                    }

                    BackupFileInfo(
                        fileName = file.name,
                        fileSize = file.length(),
                        lastModified = file.lastModified(),
                        isValid = isValid
                    )
                }
                ?.sortedByDescending { it.lastModified }
                ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e("Failed to get backup files", e)
            emptyList()
        }
    }

    /**
     * 删除备份文件
     * 同时删除关联的 WAL 和 SHM 文件
     */
    fun deleteBackup(fileName: String) {
        // ✅ 防抖检查：300ms内的重复点击直接忽略
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOperationTime < OPERATION_DEBOUNCE_MS) {
            AppLogger.w("Delete operation debounced: too frequent clicks")
            return
        }

        // ✅ 冷却时间检查：操作完成后1秒内不允许新操作
        if (currentTime - lastOperationCompleteTime < OPERATION_COOLDOWN_MS) {
            _backupMessage.value = "操作过于频繁，请稍后再试"
            AppLogger.w("Delete operation in cooldown period")
            return
        }

        lastOperationTime = currentTime

        viewModelScope.launch {
            try {
                val backupDir = getBackupDirectory()
                val mainFile = File(backupDir, fileName)

                // SQLite WAL 模式会生成三个文件：
                // - .db (主数据库文件)
                // - .db-shm (共享内存索引文件)
                // - .db-wal (预写日志文件)
                val baseFileName = fileName.removeSuffix(".db")
                val shmFile = File(backupDir, "$baseFileName.db-shm")
                val walFile = File(backupDir, "$baseFileName.db-wal")

                var deletedCount = 0
                var totalFiles = 0

                // 删除主数据库文件
                if (mainFile.exists()) {
                    totalFiles++
                    if (mainFile.delete()) {
                        deletedCount++
                        AppLogger.i("Deleted main backup file: $fileName")
                    } else {
                        AppLogger.e("Failed to delete main file: $fileName")
                    }
                }

                // 删除 SHM 文件（如果存在）
                if (shmFile.exists()) {
                    totalFiles++
                    if (shmFile.delete()) {
                        deletedCount++
                        AppLogger.i("Deleted SHM file: ${shmFile.name}")
                    } else {
                        AppLogger.e("Failed to delete SHM file: ${shmFile.name}")
                    }
                }

                // 删除 WAL 文件（如果存在）
                if (walFile.exists()) {
                    totalFiles++
                    if (walFile.delete()) {
                        deletedCount++
                        AppLogger.i("Deleted WAL file: ${walFile.name}")
                    } else {
                        AppLogger.e("Failed to delete WAL file: ${walFile.name}")
                    }
                }

                // 根据删除结果设置消息
                when {
                    totalFiles == 0 -> {
                        _backupMessage.value = "❌ 备份文件不存在"
                    }
                    deletedCount == totalFiles -> {
                        _backupMessage.value = "✅ 已删除备份及关联文件 ($deletedCount 个)"

                        // ✅ 删除成功后强制数据库同步，防止WAL缓存旧数据
                        withContext(Dispatchers.IO) {
                            try {
                                AppLogger.i("Forcing database sync after backup deletion...")
                                val database = (getApplication<Application>() as EnglishApp).database

                                // 1. WAL检查点 - 清空WAL日志
                                database.openHelper.writableDatabase.query(
                                    "PRAGMA wal_checkpoint(FULL)"
                                ).use { cursor ->
                                    if (cursor.moveToFirst()) {
                                        val busy = cursor.getInt(0)
                                        val log = cursor.getInt(1)
                                        val checkpointed = cursor.getInt(2)
                                        AppLogger.i("WAL checkpoint after deletion: busy=$busy, log=$log, checkpointed=$checkpointed")
                                    }
                                }

                                // 2. 清空内存缓存
                                database.openHelper.writableDatabase.query(
                                    "PRAGMA shrink_memory"
                                ).close()

                                AppLogger.i("Database sync completed after deletion")

                                // 3. 延迟让观察者处理变更
                                delay(200)

                            } catch (e: Exception) {
                                AppLogger.w("Database sync after deletion failed: ${e.message}")
                            }
                        }

                        // ✅ 记录操作完成时间
                        lastOperationCompleteTime = System.currentTimeMillis()
                    }
                    deletedCount > 0 -> {
                        _backupMessage.value = "⚠️ 部分删除成功 ($deletedCount/$totalFiles)"
                    }
                    else -> {
                        _backupMessage.value = "❌ 删除失败"
                    }
                }

            } catch (e: Exception) {
                _backupMessage.value = "❌ 删除失败：${e.message}"
                AppLogger.e("Delete backup failed", e)
            }
        }
    }

    /**
     * 修复备份文件权限
     * 通过重新复制文件来修复权限问题
     */
    fun fixBackupPermissions(fileName: String) {
        viewModelScope.launch {
            _isBackingUp.value = true
            _backupMessage.value = null

            try {
                val backupDir = getBackupDirectory()
                val originalFile = File(backupDir, fileName)

                if (!originalFile.exists()) {
                    _backupMessage.value = "❌ 文件不存在"
                    _isBackingUp.value = false
                    return@launch
                }

                // 创建临时文件
                val tempFile = File(backupDir, "${fileName}.temp")

                // 复制内容到临时文件（这会使用当前应用的权限）
                originalFile.inputStream().use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // 删除原文件
                originalFile.delete()

                // 重命名临时文件
                if (tempFile.renameTo(originalFile)) {
                    _backupMessage.value = "✅ 权限修复成功"
                    AppLogger.i("Successfully fixed permissions for: $fileName")
                } else {
                    _backupMessage.value = "❌ 重命名失败"
                }
            } catch (e: Exception) {
                _backupMessage.value = "❌ 修复失败：${e.message}"
                AppLogger.e("Fix permissions failed", e)
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
        _needsStoragePermission.value = false
    }

    /**
     * 打开应用设置页面
     */
    fun openAppSettings() {
        permissionManager.openAppSettings()
        _needsStoragePermission.value = false
    }

    // 🥚 Easter Egg: 彩蛋交互方法

    /**
     * 处理关于页面 GIF 的点击事件
     * 连续点击6次触发彩蛋
     */
    fun onEasterEggClick() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val lastClickTime = _lastEasterEggClickTime.value

            // 如果距离上次点击超过3秒，重置计数器
            if (currentTime - lastClickTime > 3000) {
                _easterEggClickCount.value = 1
                AppLogger.d("Easter egg click reset, count: 1")
            } else {
                _easterEggClickCount.value += 1
                AppLogger.d("Easter egg click count: ${_easterEggClickCount.value}")
            }

            _lastEasterEggClickTime.value = currentTime

            // 达到6次时触发彩蛋
            if (_easterEggClickCount.value >= 6) {
                _easterEggType.value = EasterEggType.CLICK
                _showEasterEggDialog.value = true
                _easterEggClickCount.value = 0 // 重置计数器
                AppLogger.i("🥚 Easter egg triggered: CLICK")

                // 添加触觉反馈（如果有权限）
                @Suppress("MissingPermission")  // 已使用 try-catch 处理权限问题
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE)
                        as? android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            android.os.VibrationEffect.createOneShot(
                                100,
                                android.os.VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(100)
                    }
                } catch (_: Exception) {
                    // 忽略振动错误
                }
            }
        }
    }

    /**
     * 处理关于页面 GIF 的长按事件
     */
    fun onEasterEggLongPress() {
        viewModelScope.launch {
            _easterEggType.value = EasterEggType.LONGPRESS
            _showEasterEggDialog.value = true
            _easterEggClickCount.value = 0 // 重置点击计数器
            AppLogger.i("🥚 Easter egg triggered: LONGPRESS")

            // 添加触觉反馈
            @Suppress("MissingPermission")  // 已使用 try-catch 处理权限问题
            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE)
                    as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        android.os.VibrationEffect.createOneShot(
                            200,
                            android.os.VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(200)
                }
            } catch (_: Exception) {
                // 忽略振动错误
            }
        }
    }

    /**
     * 关闭彩蛋弹窗
     */
    fun dismissEasterEgg() {
        _showEasterEggDialog.value = false
        _easterEggType.value = null
        AppLogger.d("Easter egg dialog dismissed")
    }

    /**
     * 备份文件信息
     */
    data class BackupFileInfo(
        val fileName: String,
        val fileSize: Long,
        val lastModified: Long,
        val isValid: Boolean = true  // 标识备份是否有效（未损坏）
    )
}
