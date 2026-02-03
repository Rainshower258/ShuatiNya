package com.example.english.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.example.english.ui.components.ColorPicker
import com.example.english.ui.components.SliderSetting
import com.example.english.ui.components.SwitchSetting
import com.example.english.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val backgroundColor by viewModel.backgroundColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === 外观设置 ===
            item {
                Text(
                    text = "外观设置",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // 主题模式
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "主题模式",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "选择浅色或深色主题",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = themeMode == "LIGHT",
                                onClick = { viewModel.updateThemeMode("LIGHT") },
                                label = { Text("☀️ 浅色") },
                                leadingIcon = if (themeMode == "LIGHT") {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = themeMode == "DARK",
                                onClick = { viewModel.updateThemeMode("DARK") },
                                label = { Text("🌙 深色") },
                                leadingIcon = if (themeMode == "DARK") {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 字体大小
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SliderSetting(
                            title = "字体大小",
                            value = fontSize,
                            onValueChange = { viewModel.updateFontSize(it) },
                            valueRange = 12f..24f,
                            steps = 11,
                            valueDisplay = { "${it.toInt()} sp" }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 预览
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "预览效果 Preview",
                                    fontSize = fontSize.sp
                                )
                                Text(
                                    text = "这是一段示例文字用于预览字体大小效果",
                                    fontSize = (fontSize - 2).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 背景颜色
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "背景颜色",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "自定义应用背景颜色",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        ColorPicker(
                            currentColor = Color(backgroundColor),
                            onColorSelected = { color ->
                                viewModel.updateBackgroundColor(color.toArgb().toLong())
                            }
                        )
                    }
                }
            }

            // === 学习设置 ===
            item {
                Text(
                    text = "学习设置",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                StudyReminderCard(viewModel = viewModel)
            }

            // === 数据管理 ===
            item {
                Text(
                    text = "数据管理",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                BackupManagementCard(viewModel = viewModel)
            }

            // === 关于 ===
            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // GIF 图标
                        val context = LocalContext.current
                        val imageLoader = ImageLoader.Builder(context)
                            .components {
                                add(GifDecoder.Factory())
                            }
                            .build()

                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/1000022308.gif")
                                .build(),
                            imageLoader = imageLoader
                        )

                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "刷题Nya",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "版本 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "单词/刷题记忆助手",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * 学习提醒卡片
 */
@Composable
private fun StudyReminderCard(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val studyReminderEnabled by viewModel.studyReminderEnabled.collectAsState()
    val reminderTimeMillis by viewModel.reminderTimeMillis.collectAsState()
    val reminderType by viewModel.reminderType.collectAsState()
    val missingPermissions = viewModel.checkPermissions()

    // 临时状态（用于在点击"应用"前存储用户选择）
    var tempReminderType by remember { mutableStateOf(reminderType) }
    var tempReminderTimeMillis by remember { mutableStateOf(reminderTimeMillis) }
    var showTimePicker by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    // 同步已保存的设置到临时状态
    LaunchedEffect(reminderType, reminderTimeMillis) {
        if (!hasChanges) {
            tempReminderType = reminderType
            tempReminderTimeMillis = reminderTimeMillis
        }
    }

    val (hour, minute) = remember(tempReminderTimeMillis) {
        com.example.english.ui.components.parseTimeFromMillis(tempReminderTimeMillis)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "每日学习提醒",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "设置固定时间提醒学习",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = studyReminderEnabled,
                    onCheckedChange = { viewModel.updateStudyReminder(it) }
                )
            }

            // 启用时显示设置选项
            androidx.compose.animation.AnimatedVisibility(visible = studyReminderEnabled) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // 步骤1: 提醒方式选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "1️⃣ 提醒方式",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        if (tempReminderType != reminderType) {
                            Text(
                                "（待应用）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tempReminderType == "NOTIFICATION",
                            onClick = {
                                tempReminderType = "NOTIFICATION"
                                hasChanges = true
                            },
                            label = { Text("通知") },
                            leadingIcon = if (tempReminderType == "NOTIFICATION") {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = tempReminderType == "CALENDAR",
                            onClick = {
                                tempReminderType = "CALENDAR"
                                hasChanges = true
                            },
                            label = { Text("日历") },
                            leadingIcon = if (tempReminderType == "CALENDAR") {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = tempReminderType == "ALARM",
                            onClick = {
                                tempReminderType = "ALARM"
                                hasChanges = true
                            },
                            label = { Text("闹钟") },
                            leadingIcon = if (tempReminderType == "ALARM") {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 说明文字
                    Text(
                        text = when (tempReminderType) {
                            "NOTIFICATION" -> "📱 通知提醒：应用内推送通知"
                            "CALENDAR" -> "📅 日历事件：添加到系统日历"
                            "ALARM" -> "⏰ 闹钟提醒：系统闹钟响铃"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // 步骤2: 提醒时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "2️⃣ 提醒时间",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        if (tempReminderTimeMillis != reminderTimeMillis) {
                            Text(
                                "（待应用）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("选择时间：${com.example.english.ui.components.formatTime(hour, minute)}")
                    }

                    Spacer(Modifier.height(16.dp))

                    // 步骤3: 应用设置
                    Text(
                        "3️⃣ 应用设置",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 应用按钮
                        Button(
                            onClick = {
                                // 先更新类型，再更新时间
                                if (tempReminderType != reminderType) {
                                    viewModel.updateReminderType(tempReminderType)
                                }
                                if (tempReminderTimeMillis != reminderTimeMillis) {
                                    val (h, m) = com.example.english.ui.components.parseTimeFromMillis(tempReminderTimeMillis)
                                    viewModel.updateReminderTime(h, m)
                                }
                                hasChanges = false
                            },
                            enabled = hasChanges,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (hasChanges) "应用设置" else "已应用")
                        }

                        // 测试按钮
                        OutlinedButton(
                            onClick = { viewModel.testReminder() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("测试")
                        }
                    }

                    // 状态显示
                    var statusText by remember { mutableStateOf("检查中...") }
                    LaunchedEffect(reminderType, reminderTimeMillis) {
                        statusText = viewModel.getReminderStatus()
                    }

                    if (statusText.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "当前状态：",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (statusText.startsWith("✅")) {
                                    Color(0xFF4CAF50)
                                } else if (statusText.startsWith("❌")) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // 提示信息
                    if (hasChanges) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "您有未应用的更改，点击\"应用设置\"以保存",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // 权限警告
                    if (missingPermissions.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "缺少必要权限",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "部分功能可能无法正常使用",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                TextButton(onClick = { viewModel.openAppSettings() }) {
                                    Text("设置")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 时间选择器对话框
    if (showTimePicker) {
        com.example.english.ui.components.TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onConfirm = { selectedHour, selectedMinute ->
                // 更新临时状态
                val reminderManager = com.example.english.util.ReminderManager(context)
                tempReminderTimeMillis = reminderManager.calculateNextReminderTime(selectedHour, selectedMinute)
                hasChanges = true
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

/**
 * 备份管理卡片
 */
@Composable
private fun BackupManagementCard(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()
    val backupPath by viewModel.currentBackupPath.collectAsState()
    val needsStoragePermission by viewModel.needsStoragePermission.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val operationProgress by viewModel.operationProgress.collectAsState()
    var showBackupList by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showPathDialog by remember { mutableStateOf(false) }
    var backupFiles by remember { mutableStateOf<List<SettingsViewModel.BackupFileInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        backupFiles = viewModel.getBackupFiles()
    }

    // 监听备份操作完成，自动刷新列表
    LaunchedEffect(backupMessage) {
        if (backupMessage != null && !isBackingUp) {
            backupFiles = viewModel.getBackupFiles()
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "数据备份",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "本地备份学习数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 自动备份开关（保留但功能简化）
            SwitchSetting(
                title = "自动备份",
                subtitle = "应用关闭时自动备份",
                checked = settings.isAutoBackup,
                onCheckedChange = { viewModel.updateAutoBackup(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 备份路径信息

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "备份路径",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                backupPath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { showPathDialog = true }) {
                            Icon(
                                Icons.Default.Info,
                                "路径信息",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.createManualBackup()
                        // 使用 rememberCoroutineScope 刷新列表
                    },
                    enabled = !isBackingUp,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Done, null, Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(if (isBackingUp) "备份中..." else "立即备份")
                }

                OutlinedButton(
                    onClick = { showRestoreDialog = true },
                    enabled = backupFiles.isNotEmpty() && !isBackingUp,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("恢复")
                }
            }

            // 操作进度显示
            if (operationProgress.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 根据状态显示不同的图标/动画
                        when (operationState) {
                            SettingsViewModel.BackupOperationState.CHECKING,
                            SettingsViewModel.BackupOperationState.PREPARING,
                            SettingsViewModel.BackupOperationState.BACKING_UP,
                            SettingsViewModel.BackupOperationState.RESTORING,
                            SettingsViewModel.BackupOperationState.VALIDATING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            SettingsViewModel.BackupOperationState.COMPLETED -> {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            SettingsViewModel.BackupOperationState.FAILED -> {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {}
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                operationProgress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // 显示当前步骤
                            val stepText = when (operationState) {
                                SettingsViewModel.BackupOperationState.CHECKING -> "1/6 检查权限"
                                SettingsViewModel.BackupOperationState.PREPARING ->
                                    if (isBackingUp && operationProgress.contains("备份")) "2/6 准备备份"
                                    else "2/6 准备恢复"
                                SettingsViewModel.BackupOperationState.BACKING_UP -> "3/6 复制数据"
                                SettingsViewModel.BackupOperationState.RESTORING -> "4/6 恢复数据"
                                SettingsViewModel.BackupOperationState.VALIDATING ->
                                    if (isBackingUp && operationProgress.contains("备份")) "5/6 验证备份"
                                    else "3/6 验证完整性"
                                SettingsViewModel.BackupOperationState.COMPLETED -> "完成 ✓"
                                else -> ""
                            }

                            if (stepText.isNotEmpty()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    stepText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // 状态消息
            backupMessage?.let { message ->
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.startsWith("✅"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.startsWith("✅"))
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearBackupMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "关闭",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 备份列表
            if (backupFiles.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "备份历史 (${backupFiles.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                backupFiles.take(if (showBackupList) backupFiles.size else 3).forEach { backup ->
                    BackupFileItem(
                        backup = backup,
                        onRestore = {
                            viewModel.restoreBackup(backup.fileName)
                        },
                        onDelete = {
                            viewModel.deleteBackup(backup.fileName)
                            backupFiles = viewModel.getBackupFiles()
                        },
                        onFixPermissions = {
                            viewModel.fixBackupPermissions(backup.fileName)
                        }
                    )
                }

                if (backupFiles.size > 3) {
                    TextButton(
                        onClick = { showBackupList = !showBackupList },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showBackupList) "收起" else "查看全部 (${backupFiles.size})")
                        Icon(
                            if (showBackupList) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            null
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "📦 暂无备份文件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    // 恢复确认对话框
    if (showRestoreDialog && backupFiles.isNotEmpty()) {
        var selectedBackup by remember { mutableStateOf(backupFiles.first()) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复备份") },
            text = {
                Column {
                    Text("选择要恢复的备份文件：")
                    Spacer(Modifier.height(16.dp))

                    backupFiles.forEach { backup ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBackup = backup }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBackup == backup,
                                onClick = { selectedBackup = backup }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    formatBackupTime(backup.lastModified),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    formatFileSize(backup.fileSize),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "⚠️ 注意：恢复备份将覆盖当前所有数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreBackup(selectedBackup.fileName)
                        showRestoreDialog = false
                    }
                ) {
                    Text("确认恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 备份路径信息对话框
    if (showPathDialog) {
        AlertDialog(
            onDismissRequest = { showPathDialog = false },
            title = { Text("备份路径管理") },
            text = {
                Column {
                    Text(
                        "当前路径:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        backupPath,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    val pathInfo = viewModel.getBackupPathInfo()
                    Text(
                        pathInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "💡 提示",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• 备份文件存储在应用私有目录\n" +
                        "• 卸载应用会删除所有备份\n" +
                        "• 建议定期导出重要备份",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetBackupPath()
                        showPathDialog = false
                    }
                ) {
                    Text("重置路径")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPathDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }

    // 存储权限提示对话框
    if (needsStoragePermission) {
        AlertDialog(
            onDismissRequest = { /* 不允许点击外部关闭 */ },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("需要存储权限") },
            text = {
                Column {
                    Text(
                        "备份和恢复功能需要访问存储权限。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "请在应用设置中授予存储权限：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "设置 > 权限 > 存储空间",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.openAppSettings() }
                ) {
                    Text("前往设置")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 关闭权限提示，但保留错误消息
                        viewModel.clearBackupMessage()
                    }
                ) {
                    Text("稍后")
                }
            }
        )
    }
}

@Composable
private fun BackupFileItem(
    backup: SettingsViewModel.BackupFileInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onFixPermissions: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (backup.isValid)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (backup.isValid) Icons.Default.Add else Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (backup.isValid)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatBackupTime(backup.lastModified),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    if (!backup.isValid) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "⚠️ 已损坏",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
                Text(
                    formatFileSize(backup.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 恢复按钮
            IconButton(
                onClick = onRestore,
                enabled = backup.isValid  // 只有有效的备份才能恢复
            ) {
                Icon(
                    Icons.Default.Refresh,
                    "恢复",
                    tint = if (backup.isValid)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }

            // 更多菜单
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.Info,
                        "更多选项",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!backup.isValid) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("⚠️ 备份已损坏")
                                    Text(
                                        "无法恢复此备份",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            onClick = { showMenu = false },
                            enabled = false
                        )
                        HorizontalDivider()
                    }

                    DropdownMenuItem(
                        text = { Text("🔧 修复权限") },
                        onClick = {
                            showMenu = false
                            onFixPermissions()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🗑️ 删除") },
                        onClick = {
                            showMenu = false
                            showDeleteConfirm = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此备份文件吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// 辅助函数
private fun formatBackupTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / 1024f / 1024f)
    }
}

