/*
 * MIT License
 * Copyright (c) 2025 sun6 (Rainshower258)
 * See LICENSE file in the project root for full license information.
 */
package com.example.english.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun ColorPicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }

    // 从当前颜色计算HSV
    LaunchedEffect(currentColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(currentColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
    }

    Column(modifier = modifier) {
        // 色相滑块
        Text(
            text = "色相",
            style = MaterialTheme.typography.labelMedium
        )
        Slider(
            value = hue,
            onValueChange = {
                hue = it
                val newColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
                onColorSelected(Color(newColor))
            },
            valueRange = 0f..360f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 饱和度滑块
        Text(
            text = "饱和度",
            style = MaterialTheme.typography.labelMedium
        )
        Slider(
            value = saturation,
            onValueChange = {
                saturation = it
                val newColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
                onColorSelected(Color(newColor))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 亮度滑块
        Text(
            text = "亮度",
            style = MaterialTheme.typography.labelMedium
        )
        Slider(
            value = brightness,
            onValueChange = {
                brightness = it
                val newColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
                onColorSelected(Color(newColor))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 颜色预览
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(currentColor)
                .border(1.dp, MaterialTheme.colorScheme.outline)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 预设颜色
        Text(
            text = "快速选择",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presetColors) { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (color.toArgb() == currentColor.toArgb()) 3.dp else 1.dp,
                            color = if (color.toArgb() == currentColor.toArgb())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

private val presetColors = listOf(
    Color(0xFFFFFFFF), // 白色
    Color(0xFFF5F5F5), // 浅灰
    Color(0xFFE3F2FD), // 浅蓝
    Color(0xFFFFF3E0), // 浅橙
    Color(0xFFF3E5F5), // 浅紫
    Color(0xFFE8F5E9), // 浅绿
    Color(0xFFFFFDE7), // 浅黄
    Color(0xFFFFEBEE), // 浅红
)

