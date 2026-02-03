package com.example.english.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * 权限管理器
 * 负责检测和管理学习提醒功能所需的各项权限
 */
class PermissionManager(private val context: Context) {

    companion object {
        const val REQUEST_CODE_CALENDAR = 1001
        const val REQUEST_CODE_NOTIFICATION = 1002
        const val REQUEST_CODE_ALARM = 1003
        const val REQUEST_CODE_STORAGE = 1004

        // 权限数组
        val CALENDAR_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            emptyArray() // Android 13+ 使用 scoped storage，不需要这些权限
        }
    }

    /**
     * 检查所有必要权限是否已授权
     */
    fun checkAllPermissions(): Boolean {
        return checkCalendarPermission() &&
               checkNotificationPermission() &&
               checkAlarmPermission()
    }

    /**
     * 检查日历权限
     */
    fun checkCalendarPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 6.0 以下无需运行时权限
        }
    }

    /**
     * 检查通知权限 (Android 13+)
     */
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13 以下无需通知权限
        }
    }

    /**
     * 检查精确闹钟权限 (Android 12+)
     */
    fun checkAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true // Android 12 以下无需闹钟权限
        }
    }

    /**
     * 检查存储权限（用于备份和恢复）
     * Android 13+ 使用 scoped storage，访问 app 专属目录不需要权限
     */
    fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // Android 12 及以下需要存储权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13+ 访问 app 专属目录不需要权限
        }
    }

    /**
     * 获取缺失的权限列表（用于显示）
     */
    fun getMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()

        if (!checkCalendarPermission()) {
            missing.add("日历权限")
        }
        if (!checkNotificationPermission()) {
            missing.add("通知权限")
        }
        if (!checkAlarmPermission()) {
            missing.add("精确闹钟权限")
        }
        if (!checkStoragePermission()) {
            missing.add("存储权限")
        }

        return missing
    }

    /**
     * 获取缺失的权限详细说明
     */
    fun getMissingPermissionsDetail(): String {
        val permissions = getMissingPermissions()
        if (permissions.isEmpty()) {
            return ""
        }

        val details = mutableListOf<String>()
        if (!checkCalendarPermission()) {
            details.add("• 日历权限：用于添加学习提醒事件")
        }
        if (!checkNotificationPermission()) {
            details.add("• 通知权限：用于推送学习提醒")
        }
        if (!checkAlarmPermission()) {
            details.add("• 闹钟权限：用于设置精确的提醒时间")
        }
        if (!checkStoragePermission()) {
            details.add("• 存储权限：用于备份和恢复应用数据")
        }

        return details.joinToString("\n")
    }

    /**
     * 打开应用设置页面
     */
    fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打开闹钟权限设置页面 (Android 12+)
     */
    fun openAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果失败，打开通用设置页面
                openAppSettings()
            }
        }
    }

    /**
     * 判断是否需要显示权限说明
     */
    fun shouldShowPermissionRationale(): Boolean {
        return getMissingPermissions().isNotEmpty()
    }

    /**
     * 获取权限请求说明文字
     */
    fun getPermissionRationaleMessage(): String {
        val permissions = getMissingPermissions()
        if (permissions.isEmpty()) {
            return ""
        }

        return """
            为了提供完整的学习提醒功能，需要以下权限：
            
            ${getMissingPermissionsDetail()}
            
            您可以稍后在设置中开启这些权限。
        """.trimIndent()
    }
}

