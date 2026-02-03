package com.example.english.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 时间日期工具类
 * 用于复习功能的时间管理
 */
object DateTimeHelper {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    /**
     * 获取当前时间戳（毫秒）
     */
    fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 获取当前日期字符串 (yyyy-MM-dd)
     */
    fun getCurrentDateString(): String {
        return dateFormatter.format(Date())
    }

    /**
     * 将时间戳转换为日期字符串
     */
    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    /**
     * 获取指定日期的开始时间戳（00:00:00）
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取指定日期的结束时间戳（23:59:59）
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * 获取今天开始时间戳
     */
    fun getTodayStart(): Long {
        return getStartOfDay(getCurrentTimeMillis())
    }

    /**
     * 获取今天结束时间戳
     */
    fun getTodayEnd(): Long {
        return getEndOfDay(getCurrentTimeMillis())
    }

    /**
     * 计算两个时间戳之间相差的天数
     */
    fun getDaysBetween(startTime: Long, endTime: Long): Int {
        val diff = endTime - startTime
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    /**
     * 在当前时间基础上增加指定天数
     */
    fun addDays(timestamp: Long, days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    /**
     * 判断时间戳是否为今天
     */
    fun isToday(timestamp: Long): Boolean {
        val todayStart = getTodayStart()
        val todayEnd = getTodayEnd()
        return timestamp in todayStart..todayEnd
    }

    /**
     * 判断时间戳是否为指定日期
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val date1 = formatDate(timestamp1)
        val date2 = formatDate(timestamp2)
        return date1 == date2
    }

    /**
     * 获取友好的日期显示
     * 例如: "今天", "昨天", "2天前", "2025-01-15"
     */
    fun getFriendlyDate(timestamp: Long): String {
        val now = getCurrentTimeMillis()
        val days = getDaysBetween(timestamp, now)

        return when {
            days == 0 -> "今天"
            days == 1 -> "昨天"
            days == 2 -> "前天"
            days in 3..7 -> "${days}天前"
            else -> formatDate(timestamp)
        }
    }
}

