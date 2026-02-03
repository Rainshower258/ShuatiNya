package com.example.english

import android.app.Application
import com.example.english.data.database.AppDatabase
import com.example.english.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EnglishApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(applicationContext)
    }

    // Application-level CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // ✅ 轻量级初始化在主线程
        AppLogger.init()
        AppLogger.i("Application started - ${BuildConfig.VERSION_NAME}")

        // ✅ 数据库初始化移到后台线程（避免主线程阻塞）
        if (BuildConfig.DEBUG) {
            applicationScope.launch(Dispatchers.IO) {
                try {
                    // 在IO线程获取数据库元数据
                    val metadata = AppDatabase.getDatabaseMetadata()

                    // 切回主线程记录日志
                    withContext(Dispatchers.Main) {
                        AppLogger.d("Database initialized: ${metadata.name}")
                        AppLogger.d("Database version: ${metadata.versionTag}")
                        AppLogger.d("Creator info: ${metadata.creatorInfo}")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        AppLogger.e("Failed to initialize database metadata", e)
                    }
                }
            }
        }
    }
}

