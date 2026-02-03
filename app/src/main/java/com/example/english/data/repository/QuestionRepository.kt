package com.example.english.data.repository

import com.example.english.data.database.dao.QuestionDao
import com.example.english.data.database.entity.QuestionEntity
import com.example.english.data.database.entity.toQuestion
import com.example.english.data.database.entity.toEntity
import com.example.english.data.model.Question
import com.example.english.data.model.ImportResult
import com.example.english.data.parser.QuestionParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val questionParser: QuestionParser = QuestionParser()
) {
    /**
     * 获取指定题库的所有题目
     */
    fun getQuestionsByDeckId(deckId: Long): Flow<List<Question>> {
        return questionDao.getQuestionsByDeckId(deckId).map { entities ->
            entities.map { it.toQuestion() }
        }
    }

    /**
     * 获取指定题库的随机题目
     */
    suspend fun getRandomQuestionsFromDeck(deckId: Long, count: Int): List<Question> {
        return questionDao.getRandomQuestionsFromDeck(deckId, count).map { it.toQuestion() }
    }

    /**
     * 获取需要复习的题目
     */
    suspend fun getQuestionsForReview(currentTime: Long): List<Question> {
        return questionDao.getQuestionsForReview(currentTime).map { it.toQuestion() }
    }

    /**
     * 插入单个题目
     */
    suspend fun insertQuestion(question: Question): Long {
        return questionDao.insertQuestion(question.toEntity())
    }

    /**
     * 插入多个题目
     */
    suspend fun insertQuestions(questions: List<Question>): List<Long> {
        return questionDao.insertQuestions(questions.map { it.toEntity() })
    }

    /**
     * 更新题目
     */
    suspend fun updateQuestion(question: Question) {
        questionDao.updateQuestion(question.toEntity())
    }

    /**
     * 删除题目
     */
    suspend fun deleteQuestion(question: Question) {
        questionDao.deleteQuestion(question.toEntity())
    }

    /**
     * 从文本导入题目
     */
    suspend fun importQuestionsFromText(text: String, deckId: Long): ImportResult {
        return try {
            val questions = questionParser.parseText(text, deckId)
            if (questions.isNotEmpty()) {
                // 去重处理
                val (uniqueQuestions, duplicateCount) = deduplicateQuestions(questions, deckId)
                if (uniqueQuestions.isNotEmpty()) {
                    insertQuestions(uniqueQuestions)
                }
                ImportResult(
                    successCount = uniqueQuestions.size,
                    failureCount = 0,
                    duplicateCount = duplicateCount
                )
            } else {
                ImportResult(
                    successCount = 0,
                    failureCount = 0,
                    duplicateCount = 0,
                    errors = listOf("未能解析出任何题目")
                )
            }
        } catch (e: Exception) {
            ImportResult(
                successCount = 0,
                failureCount = 1,
                duplicateCount = 0,
                errors = listOf("导入失败: ${e.message}")
            )
        }
    }

    /**
     * 去重：按题目文本判断是否重复
     * 返回：(去重后的题目列表, 重复数量)
     */
    private suspend fun deduplicateQuestions(questions: List<Question>, deckId: Long): Pair<List<Question>, Int> {
        val uniqueQuestions = mutableListOf<Question>()
        var duplicateCount = 0

        for (question in questions) {
            // 检查数据库中是否已存在相同题目文本的题目
            val existingQuestion = questionDao.findDuplicateQuestion(deckId, question.questionText)

            if (existingQuestion == null) {
                // 不存在，添加到唯一列表
                uniqueQuestions.add(question)
            } else {
                // 已存在，计数
                duplicateCount++
            }
        }

        return Pair(uniqueQuestions, duplicateCount)
    }

    /**
     * 搜索题目
     */
    suspend fun searchQuestions(deckId: Long, keyword: String): List<Question> {
        return questionDao.searchQuestions(deckId, keyword).map { it.toQuestion() }
    }

    /**
     * 删除多个题目
     */
    suspend fun deleteQuestions(questions: List<Question>) {
        questions.forEach { question ->
            questionDao.deleteQuestion(question.toEntity())
        }
    }

    /**
     * 获取题库统计信息
     */
    suspend fun getQuestionStats(deckId: Long): QuestionStats {
        val totalQuestions = questionDao.getQuestionCountByDeckId(deckId)
        val learnedQuestions = questionDao.getLearnedQuestionCountByDeck(deckId)
        val currentTime = System.currentTimeMillis()
        val reviewQuestions = questionDao.getReviewQuestionCountByDeck(deckId, currentTime)
        val masteredQuestions = questionDao.getMasteredQuestionCountByDeck(deckId)

        return QuestionStats(
            deckId = deckId,
            totalQuestions = totalQuestions,
            learnedQuestions = learnedQuestions,
            reviewQuestions = reviewQuestions,
            masteredQuestions = masteredQuestions
        )
    }
}

/**
 * 题目统计信息
 */
data class QuestionStats(
    val deckId: Long,
    val totalQuestions: Int,
    val learnedQuestions: Int,
    val reviewQuestions: Int,
    val masteredQuestions: Int
)

