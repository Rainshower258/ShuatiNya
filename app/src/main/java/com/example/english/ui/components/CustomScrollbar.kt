/*
 * MIT License
 * Copyright (c) 2025 sun6 (Rainshower258)
 * See LICENSE file in the project root for full license information.
 */

package com.example.english.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 自定义固定滚动条组件 - Material Design 3 风格
 * 使用 Canvas 直接绘制，避免布局嵌套导致的栈溢出
 *
 * @param scrollState 滚动状态
 * @param modifier 修饰符
 * @param paddingValues 内边距（用于避开 TopBar 和底部导航）
 * @param scrollbarWidth 滚动条轨道宽度
 * @param thumbMinHeight 滑块最小高度
 * @param trackColor 轨道背景色
 * @param thumbColor 滑块颜色
 * @param thumbCornerRadius 滑块圆角
 */
@Composable
fun CustomScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    scrollbarWidth: Dp = 4.dp,
    thumbMinHeight: Dp = 48.dp,
    trackColor: Color = Color.White.copy(alpha = 0.3f),
    thumbColor: Color = Color(0xFF9E9E9E),
    thumbCornerRadius: Dp = 2.dp
) {
    // 检查是否应该显示滚动条
    if (scrollState.maxValue <= 0) return

    val density = LocalDensity.current

    // ✅ 使用 Canvas 直接绘制，完全避免布局循环
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(scrollbarWidth)
            .padding(paddingValues)
            .padding(end = 4.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (canvasHeight <= 0f || canvasWidth <= 0f) return@Canvas

        // 转换 dp 到 px
        val minHeightPx = thumbMinHeight.toPx()
        val cornerRadiusPx = thumbCornerRadius.toPx()

        // 计算滑块高度
        val viewportSize = scrollState.viewportSize.toFloat()
        val contentHeight = scrollState.maxValue.toFloat() + viewportSize

        if (contentHeight <= 0f) return@Canvas

        val viewportRatio = (viewportSize / contentHeight).coerceIn(0f, 1f)
        val thumbHeightPx = (canvasHeight * viewportRatio).coerceAtLeast(minHeightPx)

        if (thumbHeightPx > canvasHeight) return@Canvas

        // 计算滑块偏移
        val scrollRatio = if (scrollState.maxValue > 0) {
            (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        val maxOffset = canvasHeight - thumbHeightPx
        val thumbOffsetY = (maxOffset * scrollRatio).coerceIn(0f, maxOffset)

        // 绘制轨道背景
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, canvasHeight),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )

        // 绘制滑块
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(0f, thumbOffsetY),
            size = Size(canvasWidth, thumbHeightPx),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
    }
}

/**
 * 简化版 - 使用默认样式
 */
@Composable
fun CustomScrollbar(
    scrollState: ScrollState,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    CustomScrollbar(
        scrollState = scrollState,
        modifier = modifier,
        paddingValues = paddingValues
    )
}
