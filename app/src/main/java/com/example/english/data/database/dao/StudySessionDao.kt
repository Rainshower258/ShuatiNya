package com.example.english.data.database.dao

import androidx.room.*
import com.example.english.data.database.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId ORDER BY start_time DESC")
    fun getSessionsByDeckId(deckId: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE is_completed = 0 ORDER BY start_time DESC LIMIT 1")
    suspend fun getCurrentSession(): StudySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Query("UPDATE study_sessions SET completed_count = :completedCount, correct_count = :correctCount WHERE id = :sessionId")
    suspend fun updateSessionProgress(sessionId: Long, completedCount: Int, correctCount: Int)

    @Query("UPDATE study_sessions SET is_completed = 1, end_time = :endTime WHERE id = :sessionId")
    suspend fun completeSession(sessionId: Long, endTime: Long)

    // === 复习中心新增功能 ===

    // 获取所有已完成的学习记录，按时间倒序
    @Query("SELECT * FROM study_sessions WHERE is_completed = 1 ORDER BY start_time DESC")
    fun getAllCompletedSessions(): Flow<List<StudySessionEntity>>

    // 获取指定词库的已完成学习记录
    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId AND is_completed = 1 ORDER BY start_time DESC")
    suspend fun getCompletedSessionsByDeck(deckId: Long): List<StudySessionEntity>

    // 删除学习记录
    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    // 根据ID删除学习记录
    @Query("DELETE FROM study_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    // 获取词库最后一次学习时间
    @Query("SELECT MAX(start_time) FROM study_sessions WHERE deck_id = :deckId AND is_completed = 1")
    suspend fun getLastStudyTimeByDeck(deckId: Long): Long?

    // 获取所有学习记录的数量
    @Query("SELECT COUNT(*) FROM study_sessions WHERE is_completed = 1")
    suspend fun getCompletedSessionCount(): Int

    // 获取指定词库的学习记录数量
    @Query("SELECT COUNT(*) FROM study_sessions WHERE deck_id = :deckId AND is_completed = 1")
    suspend fun getCompletedSessionCountByDeck(deckId: Long): Int
}
