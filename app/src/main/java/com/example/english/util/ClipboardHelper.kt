package com.example.english.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * 剪贴板工具类
 *
 * 功能：
 * - 复制文本到剪贴板（无需权限）
 * - 读取剪贴板内容（Android 13+ 需要权限）
 */
object ClipboardHelper {

    private const val TAG = "ClipboardHelper"

    /**
     * 复制文本到剪贴板
     *
     * @param context 上下文
     * @param text 要复制的文本
     * @param label 剪贴板标签
     * @return 是否复制成功
     */
    fun copyToClipboard(
        context: Context,
        text: String,
        label: String = "Prompt"
    ): Boolean {
        return try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)
            Log.d(TAG, "复制成功: $label")
            true
        } catch (e: Exception) {
            Log.e(TAG, "复制失败", e)
            false
        }
    }

    /**
     * 读取剪贴板内容
     *
     * 注意：Android 13+ 需要 READ_CLIPBOARD 权限
     *
     * @param context 上下文
     * @return 剪贴板文本内容，失败返回 null
     */
    fun readClipboard(context: Context): String? {
        return try {
            // Android 13+ 需要权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasClipboardPermission(context)) {
                    Log.w(TAG, "无剪贴板读取权限")
                    return null
                }
            }

            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboardManager.primaryClip

            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.text?.toString()
                Log.d(TAG, "读取剪贴板成功，长度: ${text?.length ?: 0}")
                text
            } else {
                Log.d(TAG, "剪贴板为空")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取剪贴板失败", e)
            null
        }
    }

    /**
     * 检查是否有剪贴板读取权限
     *
     * @param context 上下文
     * @return Android 13+ 返回权限状态，之前版本返回 true
     */
    fun hasClipboardPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                "android.permission.READ_CLIPBOARD"
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 及以下不需要权限
            true
        }
    }

    /**
     * 是否需要请求剪贴板权限
     *
     * @param context 上下文
     * @return 是否需要请求权限
     */
    fun shouldRequestPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasClipboardPermission(context)
    }
}

