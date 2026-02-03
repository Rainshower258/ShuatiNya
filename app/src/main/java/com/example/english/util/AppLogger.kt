package com.example.english.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.english.BuildConfig

/**
 * 应用日志工具类
 *
 * 🥚 Easter Egg #2: ASCII艺术创作者签名
 * 在Debug模式下启动时显示创作者信息
 *
 * @author sun6
 * @since 2025-11-13
 */
object AppLogger {

    private const val TAG = "刷题Nya"
    private const val TAG_CREATOR = "🎨Creator"

    // 是否已经显示过启动横幅
    private var bannerShown = false

    /**
     * 初始化日志系统
     * 在Debug模式下显示创作者ASCII艺术签名
     * ✅ 使用Handler延迟显示，避免阻塞应用启动
     */
    fun init() {
        if (BuildConfig.DEBUG && !bannerShown) {
            // ✅ 延迟500ms显示横幅，让UI先渲染
            Handler(Looper.getMainLooper()).postDelayed({
                showCreatorBanner()
                bannerShown = true
            }, 500) // 延迟500ms
        }
    }

    /**
     * 显示创作者横幅（仅Debug模式）
     */
    private fun showCreatorBanner() {
        val banner = """
            
            ╔═══════════════════════════════════════════════════════╗
            ║                                                       ║
            ║        🌟 刷题Nya - 单词/刷题记忆助手                 ║
            ║                                                       ║
            ║   ┌─────────────────────────────────────────────┐   ║
            ║   │                                             │   ║
            ║   │   Made with ❤️  by sun6                     │   ║
            ║   │   GitHub: @Rainshower258                    │   ║
            ║   │   Year: 2026                                │   ║
            ║   │                                             │   ║
            ║   │   "每道题都是通往梦想的阶梯，              │   ║
            ║   │    每个单词都是知识的基石。"               │   ║
            ║   │                                             │   ║
            ║   └─────────────────────────────────────────────┘   ║
            ║                                                       ║
            ║   🚀 App Version: ${BuildConfig.VERSION_NAME.padEnd(32)}║
            ║   🔧 Build Type: Debug                                ║
            ║   📱 Package: com.example.english                     ║
            ║                                                       ║
            ╚═══════════════════════════════════════════════════════╝
            
        """.trimIndent()

        Log.d(TAG_CREATOR, banner)

        // 额外的创作者信息
        logCreatorDetails()
    }

    /**
     * 记录详细的创作者信息
     */
    private fun logCreatorDetails() {
        Log.d(TAG_CREATOR, "═══════════════════════════════════════")
        Log.d(TAG_CREATOR, "📝 Creator Details:")
        Log.d(TAG_CREATOR, "   • Name: sun6")
        Log.d(TAG_CREATOR, "   • GitHub: Rainshower258")
        Log.d(TAG_CREATOR, "   • Signature: ${CreatorSignature.decodeBase64Signature()}")
        Log.d(TAG_CREATOR, "   • Fingerprint: ${CreatorSignature.generateFingerprint()}")
        Log.d(TAG_CREATOR, "   • Project Born: ${formatTimestamp(CreatorSignature.getProjectBirthDate())}")
        Log.d(TAG_CREATOR, "═══════════════════════════════════════")
    }

    /**
     * Debug级别日志
     */
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    /**
     * Info级别日志
     */
    fun i(message: String, tag: String = TAG) {
        Log.i(tag, message)
    }

    /**
     * Warning级别日志
     */
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }

    /**
     * Error级别日志
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    /**
     * 显示彩蛋发现消息
     */
    fun easterEggFound(eggName: String) {
        val message = """
            
            ╔═══════════════════════════════════════╗
            ║   🥚 EASTER EGG DISCOVERED! 🥚       ║
            ║                                       ║
            ║   You found: $eggName${" ".repeat(maxOf(0, 24 - eggName.length))} ║
            ║                                       ║
            ║   Congratulations! 🎉                 ║
            ║   - sun6                              ║
            ╚═══════════════════════════════════════╝
            
        """.trimIndent()

        Log.d(TAG_CREATOR, message)
    }

    /**
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    /**
     * ASCII艺术 - sun6 logo (小型版本)
     */
    fun showMiniLogo() {
        val logo = """
            
             ___  _   _ _ __   / /__ 
            / __|| | | | '_ \ / / _ \
            \__ \| |_| | | | / /  __/
            |___/ \__,_|_| |_\/ \___|
            
            Made with ❤️ by sun6
            
        """.trimIndent()

        Log.d(TAG_CREATOR, logo)
    }
}

