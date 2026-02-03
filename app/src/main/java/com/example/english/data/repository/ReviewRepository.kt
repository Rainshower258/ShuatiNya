package com.example.english.data.repository

import com.example.english.data.database.dao.DeckDao
import com.example.english.data.database.dao.StudySessionDao
import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.entity.toDeck
import com.example.english.data.model.DeckReviewInfo
import com.example.english.data.model.DeckStudyRecord
import com.example.english.util.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

/**
 * 复习中心专用 Repository
 * 处理按词库分组的复习数据和学习记录
 */
class ReviewRepository(
    private val deckDao: DeckDao,
    private val wordDao: WordDao,
    private val studySessionDao: StudySessionDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * 获取所有词库的复习信息（用于复习中心主界面）
     */
    suspend fun getAllDeckReviewInfo(): List<DeckReviewInfo> {
        AppLogger.d("getAllDeckReviewInfo() called")
        return try {
            // 获取今日的起始和结束时间戳
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val todayStart = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val todayEnd = calendar.timeInMillis

            AppLogger.d("Today range: $todayStart - $todayEnd")

            val currentTime = System.currentTimeMillis()

            // 获取所有词库
            val decks = deckDao.getAllDecks().first()
            AppLogger.d("Found ${decks.size} decks")

            val deckList = mutableListOf<DeckReviewInfo>()

            for (deck in decks) {
                val deckId = deck.id
                AppLogger.d("Processing deck: ${deck.name} (id=$deckId)")

                try {
                    // 获取统计数据
                    val totalWords = wordDao.getWordCountByDeckId(deckId)
                    AppLogger.d("  totalWords: $totalWords")

                    val learnedWords = wordDao.getLearnedWordCountByDeck(deckId)
                    AppLogger.d("  learnedWords: $learnedWords")

                    val reviewWords = wordDao.getReviewWordCountByDeck(deckId, currentTime)
                    AppLogger.d("  reviewWords: $reviewWords")

                    val masteredWords = wordDao.getMasteredWordCountByDeck(deckId)
                    AppLogger.d("  masteredWords: $masteredWords")

                    val todayLearned = wordDao.getTodayLearnedCountByDeck(deckId, todayStart, todayEnd)
                    AppLogger.d("  todayLearned: $todayLearned")

                    val todayReviewed = wordDao.getTodayReviewedCountByDeck(deckId, todayStart, todayEnd)
                    AppLogger.d("  todayReviewed: $todayReviewed")

                    val lastStudyDate = studySessionDao.getLastStudyTimeByDeck(deckId)
                    AppLogger.d("  lastStudyDate: $lastStudyDate")

                    deckList.add(
                        DeckReviewInfo(
                            deckId = deckId,
                            deckName = deck.name,
                            totalWords = totalWords,
                            learnedWords = learnedWords,
                            reviewWords = reviewWords,
                            masteredWords = masteredWords,
                            lastStudyDate = lastStudyDate,
                            todayLearned = todayLearned,
                            todayReviewed = todayReviewed
                        )
                    )
                } catch (e: Exception) {
                    AppLogger.e("Error processing deck ${deck.name}", e)
                    // 继续处理其他词库
                }
            }

            AppLogger.d("getAllDeckReviewInfo() completed, returning ${deckList.size} items")
            deckList
        } catch (e: Exception) {
            AppLogger.e("Fatal error in getAllDeckReviewInfo", e)
            throw e
        }
    }

    /**
     * 获取单个词库的复习信息
     */
    suspend fun getDeckReviewInfo(deckId: Long): DeckReviewInfo? {
        val deck = deckDao.getDeckById(deckId)?.toDeck() ?: return null

        // 获取今日的起始和结束时间戳
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val todayEnd = calendar.timeInMillis

        val currentTime = System.currentTimeMillis()

        // 获取统计数据
        val totalWords = wordDao.getWordCountByDeckId(deckId)
        val learnedWords = wordDao.getLearnedWordCountByDeck(deckId)
        val reviewWords = wordDao.getReviewWordCountByDeck(deckId, currentTime)
        val masteredWords = wordDao.getMasteredWordCountByDeck(deckId)
        val todayLearned = wordDao.getTodayLearnedCountByDeck(deckId, todayStart, todayEnd)
        val todayReviewed = wordDao.getTodayReviewedCountByDeck(deckId, todayStart, todayEnd)
        val lastStudyDate = studySessionDao.getLastStudyTimeByDeck(deckId)

        return DeckReviewInfo(
            deckId = deckId,
            deckName = deck.name,
            totalWords = totalWords,
            learnedWords = learnedWords,
            reviewWords = reviewWords,
            masteredWords = masteredWords,
            lastStudyDate = lastStudyDate,
            todayLearned = todayLearned,
            todayReviewed = todayReviewed
        )
    }

    /**
     * 获取指定词库的学习记录列表
     */
    suspend fun getDeckStudyRecords(deckId: Long): List<DeckStudyRecord> {
        val deck = deckDao.getDeckById(deckId)?.toDeck() ?: return emptyList()
        val sessions = studySessionDao.getCompletedSessionsByDeck(deckId)

        return sessions.map { session ->
            val accuracy = if (session.completedCount > 0) {
                session.correctCount.toFloat() / session.completedCount.toFloat()
            } else {
                0f
            }

            DeckStudyRecord(
                sessionId = session.id,
                deckId = session.deckId,
                deckName = deck.name,
                date = dateFormat.format(Date(session.startTime)),
                timestamp = session.startTime,
                plannedCount = session.plannedCount,
                completedCount = session.completedCount,
                correctCount = session.correctCount,
                accuracy = accuracy
            )
        }
    }

    /**
     * 获取所有学习记录（跨词库）
     */
    fun getAllStudyRecords(): Flow<List<DeckStudyRecord>> {
        return studySessionDao.getAllCompletedSessions().map { sessions ->
            sessions.mapNotNull { session ->
                val deck = deckDao.getDeckById(session.deckId)?.toDeck()
                deck?.let {
                    val accuracy = if (session.completedCount > 0) {
                        session.correctCount.toFloat() / session.completedCount.toFloat()
                    } else {
                        0f
                    }

                    DeckStudyRecord(
                        sessionId = session.id,
                        deckId = session.deckId,
                        deckName = deck.name,
                        date = dateFormat.format(Date(session.startTime)),
                        timestamp = session.startTime,
                        plannedCount = session.plannedCount,
                        completedCount = session.completedCount,
                        correctCount = session.correctCount,
                        accuracy = accuracy
                    )
                }
            }
        }
    }

    /**
     * 删除学习记录
     */
    suspend fun deleteStudyRecord(sessionId: Long) {
        studySessionDao.deleteSessionById(sessionId)
    }

    /**
     * 获取学习记录总数
     */
    suspend fun getStudyRecordCount(): Int {
        return studySessionDao.getCompletedSessionCount()
    }

    /**
     * 获取指定词库的学习记录总数
     */
    suspend fun getDeckStudyRecordCount(deckId: Long): Int {
        return studySessionDao.getCompletedSessionCountByDeck(deckId)
    }
}

