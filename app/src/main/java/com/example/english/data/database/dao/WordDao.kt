package com.example.english.data.database.dao

import androidx.room.*
import com.example.english.data.database.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE deck_id = :deckId ORDER BY created_at DESC")
    fun getWordsByDeckId(deckId: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE deck_id = :deckId ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsFromDeck(deckId: Long, count: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE deck_id = :deckId AND wrong_count > correct_count ORDER BY wrong_count DESC")
    suspend fun getDifficultWordsFromDeck(deckId: Long): List<WordEntity>

    @Query("SELECT * FROM words WHERE next_review_time <= :currentTime ORDER BY next_review_time")
    suspend fun getWordsForReview(currentTime: Long): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE deck_id = :deckId")
    suspend fun getWordCountByDeckId(deckId: Long): Int

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE deck_id = :deckId AND english = :english AND part_of_speech = :partOfSpeech LIMIT 1")
    suspend fun findDuplicateWord(deckId: Long, english: String, partOfSpeech: String): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Update
    suspend fun updateWord(word: WordEntity)

    @Update
    suspend fun updateWords(words: List<WordEntity>)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("DELETE FROM words WHERE deck_id = :deckId")
    suspend fun deleteWordsByDeckId(deckId: Long)

    @Query("UPDATE words SET correct_count = correct_count + 1, last_review_time = :reviewTime, next_review_time = :nextReviewTime, ease_factor = :easeFactor, interval_days = :intervalDays, repetition = :repetition WHERE id = :wordId")
    suspend fun updateWordCorrect(
        wordId: Long,
        reviewTime: Long,
        nextReviewTime: Long,
        easeFactor: Float,
        intervalDays: Int,
        repetition: Int
    )

    @Query("UPDATE words SET wrong_count = wrong_count + 1, last_review_time = :reviewTime, next_review_time = :nextReviewTime, ease_factor = :easeFactor, interval_days = :intervalDays, repetition = :repetition WHERE id = :wordId")
    suspend fun updateWordWrong(
        wordId: Long,
        reviewTime: Long,
        nextReviewTime: Long,
        easeFactor: Float,
        intervalDays: Int,
        repetition: Int
    )

    // 获取其他词库的随机单词用作干扰项
    @Query("SELECT * FROM words WHERE deck_id != :excludeDeckId ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsForDistractors(excludeDeckId: Long, count: Int): List<WordEntity>

    // 从当前词库获取随机单词作为干扰项（排除当前单词）
    @Query("SELECT * FROM words WHERE deck_id = :deckId AND id != :excludeWordId ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsFromSameDeck(deckId: Long, excludeWordId: Long, count: Int): List<WordEntity>

    // 获取已学习的单词（复习时间大于0表示已学习）
    @Query("SELECT * FROM words WHERE deck_id = :deckId AND last_review_time > 0 ORDER BY last_review_time DESC")
    suspend fun getStudiedWords(deckId: Long): List<WordEntity>

    // === 复习功能新增查询 ===

    // 获取今日学习的单词
    @Query("SELECT * FROM words WHERE first_learn_date >= :startTime AND first_learn_date < :endTime ORDER BY first_learn_date DESC")
    suspend fun getWordsLearnedOnDate(startTime: Long, endTime: Long): List<WordEntity>

    // 获取需要复习的单词（到了复习时间且复习阶段>0）
    @Query("SELECT * FROM words WHERE next_review_time > 0 AND next_review_time <= :currentTime AND review_stage > 0 ORDER BY next_review_time ASC")
    suspend fun getWordsNeedReview(currentTime: Long): List<WordEntity>

    // 获取指定词库需要复习的单词
    @Query("SELECT * FROM words WHERE deck_id = :deckId AND next_review_time > 0 AND next_review_time <= :currentTime AND review_stage > 0 ORDER BY next_review_time ASC")
    suspend fun getWordsNeedReviewByDeck(deckId: Long, currentTime: Long): List<WordEntity>

    // 更新单词的复习信息（首次学习）
    @Query("UPDATE words SET first_learn_date = :learnDate, review_stage = :stage, next_review_time = :nextReviewTime, last_review_time = :lastReviewTime WHERE id = :wordId")
    suspend fun updateWordFirstLearn(wordId: Long, learnDate: Long, stage: Int, nextReviewTime: Long, lastReviewTime: Long)

    // 更新单词的复习信息（复习记住）
    @Query("UPDATE words SET review_stage = :stage, next_review_time = :nextReviewTime, last_review_time = :lastReviewTime, correct_count = correct_count + 1 WHERE id = :wordId")
    suspend fun updateWordReviewRemembered(wordId: Long, stage: Int, nextReviewTime: Long, lastReviewTime: Long)

    // 更新单词的复习信息（复习忘记）
    @Query("UPDATE words SET review_stage = :stage, next_review_time = :nextReviewTime, last_review_time = :lastReviewTime, wrong_count = wrong_count + 1 WHERE id = :wordId")
    suspend fun updateWordReviewForgotten(wordId: Long, stage: Int, nextReviewTime: Long, lastReviewTime: Long)

    // 获取今日需要复习的单词数量
    @Query("SELECT COUNT(*) FROM words WHERE next_review_time > 0 AND next_review_time <= :currentTime AND review_stage > 0")
    suspend fun getTodayReviewCount(currentTime: Long): Int

    // 获取所有已学习但未完全掌握的单词（用于统计）
    @Query("SELECT * FROM words WHERE review_stage > 0 ORDER BY first_learn_date DESC")
    suspend fun getAllLearningWords(): List<WordEntity>

    // 按日期分组获取学习记录（用于历史展示）
    @Query("SELECT * FROM words WHERE first_learn_date > 0 ORDER BY first_learn_date DESC")
    suspend fun getAllLearnedWordsOrderByDate(): List<WordEntity>

    // === 按词库分组统计查询 ===

    // 获取词库的已学习单词数（first_learn_date > 0）
    @Query("SELECT COUNT(*) FROM words WHERE deck_id = :deckId AND first_learn_date > 0")
    suspend fun getLearnedWordCountByDeck(deckId: Long): Int

    // 获取词库的待复习单词数
    @Query("SELECT COUNT(*) FROM words WHERE deck_id = :deckId AND next_review_time > 0 AND next_review_time <= :currentTime AND review_stage > 0")
    suspend fun getReviewWordCountByDeck(deckId: Long, currentTime: Long): Int

    // 获取词库的已掌握单词数（review_stage >= 5）
    @Query("SELECT COUNT(*) FROM words WHERE deck_id = :deckId AND review_stage >= 5")
    suspend fun getMasteredWordCountByDeck(deckId: Long): Int

    // 获取词库今日学习的单词数
    @Query("SELECT COUNT(*) FROM words WHERE deck_id = :deckId AND first_learn_date >= :startTime AND first_learn_date < :endTime")
    suspend fun getTodayLearnedCountByDeck(deckId: Long, startTime: Long, endTime: Long): Int

    // 获取词库今日复习的单词（通过 last_review_time 判断）
    @Query("SELECT COUNT(*) FROM words WHERE deck_id = :deckId AND last_review_time >= :startTime AND last_review_time < :endTime AND review_stage > 0")
    suspend fun getTodayReviewedCountByDeck(deckId: Long, startTime: Long, endTime: Long): Int

    // 搜索单词（根据关键字搜索英文或中文）
    @Query("SELECT * FROM words WHERE deck_id = :deckId AND (english LIKE '%' || :keyword || '%' OR chinese LIKE '%' || :keyword || '%') ORDER BY created_at DESC")
    suspend fun searchWords(deckId: Long, keyword: String): List<WordEntity>
}
