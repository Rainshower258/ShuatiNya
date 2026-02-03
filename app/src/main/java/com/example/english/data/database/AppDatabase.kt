package com.example.english.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.english.data.database.dao.DeckDao
import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.dao.StudySessionDao
import com.example.english.data.database.dao.QuestionDao
import com.example.english.data.local.dao.SettingsDao
import com.example.english.data.database.entity.DeckEntity
import com.example.english.data.database.entity.WordEntity
import com.example.english.data.database.entity.StudySessionEntity
import com.example.english.data.database.entity.QuestionEntity
import com.example.english.data.local.entity.SettingsEntity

@Database(
    entities = [WordEntity::class, DeckEntity::class, StudySessionEntity::class, SettingsEntity::class, QuestionEntity::class],
    version = 6,
    exportSchema = true  // M-6: 启用 schema 导出，便于追踪数据库变更
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun deckDao(): DeckDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 🥚 Easter Egg #3: 数据库名称包含创作者标识
        private const val DATABASE_NAME = "english_learning_sun6_db"

        // 创作者元数据 (Base64编码)
        private const val CREATOR_META = "Y3JlYXRvcjpzdW42fHllYXI6MjAyNXxnaXRodWI6UmFpbnNob3dlcjI1OA=="

        // 数据库版本标识
        private const val DB_VERSION_TAG = "V6.0_SUN6_BUILD"

        // 数据库迁移：版本2到版本3，添加复习功能字段
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加首次学习日期字段
                database.execSQL("ALTER TABLE words ADD COLUMN first_learn_date INTEGER NOT NULL DEFAULT 0")
                // 添加复习阶段字段
                database.execSQL("ALTER TABLE words ADD COLUMN review_stage INTEGER NOT NULL DEFAULT 0")
            }
        }

        // 数据库迁移：版本3到版本4，添加学习提醒功能字段
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为 settings 表添加提醒相关字段
                database.execSQL("ALTER TABLE settings ADD COLUMN reminderTimeMillis INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE settings ADD COLUMN reminderType TEXT NOT NULL DEFAULT 'NOTIFICATION'")
                database.execSQL("ALTER TABLE settings ADD COLUMN calendarEventId INTEGER")
                database.execSQL("ALTER TABLE settings ADD COLUMN alarmRequestCode INTEGER")
            }
        }

        // 数据库迁移：版本4到版本5，添加短语支持字段
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为 words 表添加短语相关字段
                database.execSQL("ALTER TABLE words ADD COLUMN word_type TEXT NOT NULL DEFAULT 'WORD'")
                database.execSQL("ALTER TABLE words ADD COLUMN phrase_usage TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME  // 使用包含创作者标识的数据库名
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    // 不使用 fallbackToDestructiveMigration()，确保用户数据安全
                    // 未来数据库结构变更时，必须编写对应的 Migration 脚本
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 清除数据库实例
         * ⚠️ 已废弃：会导致运行时数据库连接失效，引发崩溃
         * @deprecated 此方法会破坏单例模式，导致已注入的依赖引用失效。请使用数据迁移方式恢复备份。
         */
        @Deprecated(
            message = "此方法会破坏运行时数据库连接，导致应用崩溃。请使用数据迁移方式恢复备份，避免关闭全局数据库实例。",
            level = DeprecationLevel.ERROR
        )
        @Synchronized
        fun clearInstance() {
            INSTANCE?.let {
                if (it.isOpen) {
                    try {
                        it.close()
                        android.util.Log.i("刷题Nya", "Database instance closed")
                    } catch (e: Exception) {
                        android.util.Log.e("刷题Nya", "Error closing database", e)
                    }
                }
            }
            INSTANCE = null
            android.util.Log.i("刷题Nya", "Database instance cleared")
        }

        /**
         * 强制重新初始化数据库
         * ⚠️ 已废弃：会导致运行时数据库连接失效，引发崩溃
         * @deprecated 此方法会破坏运行时数据库连接，导致应用崩溃。请使用数据迁移方式恢复备份，避免关闭全局数据库实例。
         */
        @Deprecated(
            message = "此方法会破坏运行时数据库连接，导致应用崩溃。请使用数据迁移方式恢复备份，避免关闭全局数据库实例。",
            level = DeprecationLevel.ERROR
        )
        @Synchronized
        @Suppress("DEPRECATION_ERROR")  // 抑制对已废弃方法的调用
        fun reinitialize(context: Context): AppDatabase {
            clearInstance()
            android.util.Log.i("刷题Nya", "Reinitializing database...")
            return getDatabase(context)
        }

        /**
         * 获取创作者信息 (解码Base64元数据)
         * 🥚 Easter Egg: 隐藏的创作者信息
         */
        fun getCreatorInfo(): String {
            return String(android.util.Base64.decode(CREATOR_META, android.util.Base64.DEFAULT))
        }

        /**
         * 获取数据库元数据
         */
        fun getDatabaseMetadata(): DatabaseMetadata {
            val creatorInfo = getCreatorInfo()
            return DatabaseMetadata(
                name = DATABASE_NAME,
                version = 6,
                versionTag = DB_VERSION_TAG,
                creatorInfo = creatorInfo
            )
        }
    }
}

/**
 * 数据库元数据
 */
data class DatabaseMetadata(
    val name: String,
    val version: Int,
    val versionTag: String,
    val creatorInfo: String
)

