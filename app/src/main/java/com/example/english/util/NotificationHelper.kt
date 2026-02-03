package com.example.english.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.english.MainActivity

/**
 * 通知帮助类
 * 负责创建和显示学习提醒通知
 */
object NotificationHelper {

    const val CHANNEL_ID = "study_reminder_channel"
    const val CHANNEL_NAME = "学习提醒"
    const val NOTIFICATION_ID = 1001

    /**
     * 创建通知渠道（Android 8.0+）
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "每日学习提醒通知"
                enableVibration(true)
                enableLights(true)

                // 设置通知声音
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * 显示学习提醒通知
     */
    fun showStudyNotification(context: Context) {
        // 确保通知渠道已创建
        createNotificationChannel(context)

        // 创建点击通知的意图
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 使用系统默认图标
            .setContentTitle("📚 学习时间到了！")
            .setContentText("坚持每日学习，提升英语水平")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("现在是学习英语的好时机！\n坚持每日学习，进步看得见 💪")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // 显示通知
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 取消通知
     */
    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }
}

