package com.example.english

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.english.ui.navigation.AppNavigation
import com.example.english.ui.theme.EnglishTheme
import com.example.english.ui.viewmodel.SettingsViewModel
import com.example.english.util.PermissionManager

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager
    private var hasShownPermissionDialog = false
    private var showPermissionDialog by mutableStateOf(false)
    private var permissionDialogMessage by mutableStateOf("")

    private var showAlarmPermissionDialog by mutableStateOf(false)

    // 存储权限请求启动器（Android 12 及以下）
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        handlePermissionResult("存储", allGranted)
    }

    // 日历权限请求启动器
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        handlePermissionResult("日历", allGranted)

        // 继续请求其他权限
        if (allGranted) {
            requestNotificationPermissionIfNeeded()
        }
    }

    // 通知权限请求启动器 (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        handlePermissionResult("通知", granted)

        // 继续请求闹钟权限
        if (granted) {
            checkAlarmPermissionIfNeeded()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val fontSize by settingsViewModel.fontSize.collectAsState(initial = 16f)
            val backgroundColor by settingsViewModel.backgroundColor.collectAsState(initial = 0xFFFFFFFF)
            val themeMode by settingsViewModel.themeMode.collectAsState(initial = "LIGHT")

            EnglishTheme(
                themeMode = themeMode,
                fontSize = fontSize,
                backgroundColor = Color(backgroundColor)
            ) {
                AppNavigation()
            }

            // 权限对话框
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("需要授权") },
                    text = { Text(permissionDialogMessage) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPermissionDialog = false
                                markPermissionsChecked()
                                requestPermissionsSequentially()
                            }
                        ) {
                            Text("立即授权")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showPermissionDialog = false
                                markPermissionsChecked()
                                showPermissionWarningToast()
                            }
                        ) {
                            Text("稍后")
                        }
                    }
                )
            }

            // 闹钟权限对话框
            if (showAlarmPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("需要闹钟权限") },
                    text = { Text("为了使用精确闹钟提醒功能，需要授权精确闹钟权限。\n\n点击\"去设置\"将跳转到系统设置页面进行授权。") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showAlarmPermissionDialog = false
                                permissionManager.openAlarmSettings()
                            }
                        ) {
                            Text("去设置")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAlarmPermissionDialog = false
                                showPermissionWarningToast()
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }
        }

        // 首次启动检查权限
        checkInitialPermissions()
    }

    /**
     * 首次启动检查权限
     */
    private fun checkInitialPermissions() {
        // 只在首次启动时检查
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasCheckedPermissions = prefs.getBoolean("has_checked_permissions", false)

        if (!hasCheckedPermissions && !hasShownPermissionDialog) {
            val missingPermissions = permissionManager.getMissingPermissions()

            if (missingPermissions.isNotEmpty()) {
                hasShownPermissionDialog = true
                permissionDialogMessage = permissionManager.getPermissionRationaleMessage()
                showPermissionDialog = true
            } else {
                markPermissionsChecked()
            }
        }
    }

    /**
     * 标记权限已检查
     */
    private fun markPermissionsChecked() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("has_checked_permissions", true)
            .apply()
    }

    /**
     * 按顺序请求权限
     */
    private fun requestPermissionsSequentially() {
        // 1. 先请求存储权限（Android 12 及以下）
        if (!permissionManager.checkStoragePermission() && PermissionManager.STORAGE_PERMISSIONS.isNotEmpty()) {
            storagePermissionLauncher.launch(PermissionManager.STORAGE_PERMISSIONS)
        } else if (!permissionManager.checkCalendarPermission()) {
            // 2. 请求日历权限
            calendarPermissionLauncher.launch(PermissionManager.CALENDAR_PERMISSIONS)
        } else {
            // 日历权限已有，请求通知权限
            requestNotificationPermissionIfNeeded()
        }
    }

    /**
     * 请求通知权限（如果需要）
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionManager.checkNotificationPermission()) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkAlarmPermissionIfNeeded()
            }
        } else {
            checkAlarmPermissionIfNeeded()
        }
    }

    /**
     * 检查闹钟权限（如果需要）
     */
    private fun checkAlarmPermissionIfNeeded() {
        if (!permissionManager.checkAlarmPermission()) {
            showAlarmPermissionDialog = true
        }
    }

    /**
     * 处理权限结果
     */
    private fun handlePermissionResult(permissionName: String, granted: Boolean) {
        if (granted) {
            Toast.makeText(this, "$permissionName 权限已授权", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "$permissionName 权限被拒绝，部分功能可能无法使用", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * 显示权限警告提示
     */
    private fun showPermissionWarningToast() {
        Toast.makeText(
            this,
            "未获取到必要权限，部分功能无法正常运行\n可在设置中手动开启",
            Toast.LENGTH_LONG
        ).show()
    }
}
