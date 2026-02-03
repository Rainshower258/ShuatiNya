package com.example.english.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学习会话记录
 */
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "deck_id")
    val deckId: Long,

    @ColumnInfo(name = "planned_count")
    val plannedCount: Int, // 计划学习数量

    @ColumnInfo(name = "completed_count")
    val completedCount: Int = 0, // 已完成数量

    @ColumnInfo(name = "correct_count")
    val correctCount: Int = 0, // 正确数量

    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)
