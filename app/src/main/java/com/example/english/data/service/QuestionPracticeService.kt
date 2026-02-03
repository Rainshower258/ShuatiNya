package com.example.english.data.service

import com.example.english.data.database.dao.QuestionDao
import com.example.english.data.database.entity.QuestionEntity
import com.example.english.data.model.Question
import com.example.english.data.model.QuestionStudyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 刷题服务
 * 管理题目练习逻辑
 */
class QuestionPracticeService(
    private val questionDao: QuestionDao
) {
    private val _currentQuestions = MutableStateFlow<List<QuestionStudyState>>(emptyList())
    val currentQuestions: StateFlow<List<QuestionStudyState>> = _currentQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _isPracticeSessionActive = MutableStateFlow(false)
    val isPracticeSessionActive: StateFlow<Boolean> = _isPracticeSessionActive.asStateFlow()

    /**
     * 开始练习会话
     */
    suspend fun startPracticeSession(deckId: Long, plannedCount: Int): Result<List<QuestionStudyState>> {
        return try {
            // 获取要练习的题目
            val questions = selectQuestionsForPractice(deckId, plannedCount)
            val studyStates = questions.map { question ->
                QuestionStudyState(question = question.toQuestion())
            }

            _currentQuestions.value = studyStates
            _currentQuestionIndex.value = 0
            _isPracticeSessionActive.value = true

            Result.success(studyStates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 选择要练习的题目
     */
    private suspend fun selectQuestionsForPractice(deckId: Long, count: Int): List<QuestionEntity> {
        val currentTime = System.currentTimeMillis()

        // 优先获取需要复习的题目
        val reviewQuestions = questionDao.getQuestionsNeedReview(deckId, currentTime)

        return if (reviewQuestions.size >= count) {
            reviewQuestions.take(count)
        } else {
            // 不足则补充随机题目
            val additionalCount = count - reviewQuestions.size
            val reviewQuestionIds = reviewQuestions.map { it.id }.toSet()

            // 获取更多候选题目以确保去重后仍有足够数量
            val allRandomQuestions = questionDao.getRandomQuestionsFromDeck(deckId, additionalCount * 2)
            val randomQuestions = allRandomQuestions
                .filter { it.id !in reviewQuestionIds }  // 去重：排除已在复习列表中的题目
                .take(additionalCount)

            (reviewQuestions + randomQuestions).take(count)
        }
    }

    /**
     * 提交答案
     */
    suspend fun submitAnswer(questionId: Long, userAnswer: String): Boolean {
        val currentList = _currentQuestions.value.toMutableList()
        val index = currentList.indexOfFirst { it.question.id == questionId }

        if (index != -1) {
            val state = currentList[index]
            val isCorrect = state.question.correctAnswer == userAnswer

            currentList[index] = state.copy(
                isCorrect = isCorrect,
                userAnswer = userAnswer,
                isCompleted = true
            )
            _currentQuestions.value = currentList

            // 更新数据库中的题目统计
            updateQuestionStats(questionId, isCorrect)

            return isCorrect
        }

        return false
    }

    /**
     * 更新题目统计
     */
    private suspend fun updateQuestionStats(questionId: Long, isCorrect: Boolean) {
        val question = questionDao.getQuestionById(questionId) ?: return

        val updatedQuestion = if (isCorrect) {
            question.copy(
                correctCount = question.correctCount + 1,
                lastReviewTime = System.currentTimeMillis(),
                firstLearnDate = if (question.firstLearnDate == 0L) System.currentTimeMillis() else question.firstLearnDate
            )
        } else {
            question.copy(
                wrongCount = question.wrongCount + 1,
                lastReviewTime = System.currentTimeMillis(),
                firstLearnDate = if (question.firstLearnDate == 0L) System.currentTimeMillis() else question.firstLearnDate
            )
        }

        questionDao.updateQuestion(updatedQuestion)
    }

    /**
     * 移动到下一题
     */
    fun moveToNextQuestion() {
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex < _currentQuestions.value.size - 1) {
            _currentQuestionIndex.value = currentIndex + 1
        }
    }

    /**
     * 移动到上一题
     */
    fun moveToPreviousQuestion() {
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex > 0) {
            _currentQuestionIndex.value = currentIndex - 1
        }
    }

    /**
     * 完成练习会话
     */
    fun completePracticeSession() {
        _isPracticeSessionActive.value = false
    }

    /**
     * 重置服务状态
     */
    fun reset() {
        _currentQuestions.value = emptyList()
        _currentQuestionIndex.value = 0
        _isPracticeSessionActive.value = false
    }
}

private fun QuestionEntity.toQuestion(): Question {
    return Question(
        id = id,
        deckId = deckId,
        questionType = com.example.english.data.model.QuestionType.valueOf(questionType),
        questionText = questionText,
        optionA = optionA,
        optionB = optionB,
        optionC = optionC,
        optionD = optionD,
        correctAnswer = correctAnswer,
        createdAt = createdAt,
        correctCount = correctCount,
        wrongCount = wrongCount,
        lastReviewTime = lastReviewTime,
        nextReviewTime = nextReviewTime,
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetition = repetition,
        firstLearnDate = firstLearnDate,
        reviewStage = reviewStage
    )
}

