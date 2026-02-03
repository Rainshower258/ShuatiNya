package com.example.english.data.database.dao

import androidx.room.*
import com.example.english.data.database.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE deck_id = :deckId ORDER BY created_at DESC")
    fun getQuestionsByDeckId(deckId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE deck_id = :deckId ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsFromDeck(deckId: Long, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE deck_id = :deckId AND wrong_count > correct_count ORDER BY wrong_count DESC")
    suspend fun getDifficultQuestionsFromDeck(deckId: Long): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE next_review_time <= :currentTime ORDER BY next_review_time")
    suspend fun getQuestionsForReview(currentTime: Long): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE deck_id = :deckId")
    suspend fun getQuestionCountByDeckId(deckId: Long): Int

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE deck_id = :deckId")
    suspend fun deleteQuestionsByDeckId(deckId: Long)

    // SRS相关查询
    @Query("SELECT * FROM questions WHERE deck_id = :deckId AND next_review_time <= :currentTime")
    suspend fun getQuestionsNeedReview(deckId: Long, currentTime: Long): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE deck_id = :deckId AND first_learn_date > 0")
    suspend fun getLearnedQuestionCountByDeck(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE deck_id = :deckId AND next_review_time <= :currentTime AND next_review_time > 0")
    suspend fun getReviewQuestionCountByDeck(deckId: Long, currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE deck_id = :deckId AND review_stage > 5")
    suspend fun getMasteredQuestionCountByDeck(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE deck_id = :deckId AND first_learn_date >= :startTime AND first_learn_date <= :endTime")
    suspend fun getTodayLearnedCountByDeck(deckId: Long, startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE deck_id = :deckId AND last_review_time >= :startTime AND last_review_time <= :endTime")
    suspend fun getTodayReviewedCountByDeck(deckId: Long, startTime: Long, endTime: Long): Int

    @Query("SELECT * FROM questions WHERE first_learn_date > 0 ORDER BY first_learn_date DESC")
    suspend fun getAllLearnedQuestionsOrderByDate(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE first_learn_date >= :startTime AND first_learn_date <= :endTime")
    suspend fun getQuestionsLearnedOnDate(startTime: Long, endTime: Long): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE first_learn_date > 0")
    suspend fun getAllLearningQuestions(): List<QuestionEntity>

    // 查找重复题目（根据题目文本判断）
    @Query("SELECT * FROM questions WHERE deck_id = :deckId AND question_text = :questionText LIMIT 1")
    suspend fun findDuplicateQuestion(deckId: Long, questionText: String): QuestionEntity?

    // 搜索题目（根据关键字搜索题目文本）
    @Query("SELECT * FROM questions WHERE deck_id = :deckId AND question_text LIKE '%' || :keyword || '%' ORDER BY created_at DESC")
    suspend fun searchQuestions(deckId: Long, keyword: String): List<QuestionEntity>
}

