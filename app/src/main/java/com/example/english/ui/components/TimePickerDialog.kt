package com.example.english.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties
import java.util.*

/**
 * 时间选择器对话框
 * 用于选择提醒时间
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int = 9,
    initialMinute: Int = 0,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors()
            )
        }
    )
}

/**
 * 从时间戳解析小时和分钟
 */
fun parseTimeFromMillis(timeMillis: Long): Pair<Int, Int> {
    if (timeMillis == 0L) {
        return Pair(9, 0) // 默认 9:00
    }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = timeMillis
    }

    return Pair(
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    )
}

/**
 * 格式化时间显示
 */
fun formatTime(hour: Int, minute: Int): String {
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

