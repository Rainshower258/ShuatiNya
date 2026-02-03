package com.example.english.data.service

import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.dao.StudySessionDao
import com.example.english.data.database.entity.StudySessionEntity
import com.example.english.data.database.entity.WordEntity
import com.example.english.data.database.entity.toWord
import com.example.english.data.model.Word
import com.example.english.data.model.ChoiceOption
import com.example.english.data.model.WordStudyState
import com.example.english.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 学习阶段枚举
 */
enum class StudyPhase {
    MAIN_STUDY,        // 主要学习阶段
    PHRASE_REVIEW,     // 短语复习阶段
    WRONG_WORD_REVIEW, // 错题复习阶段
    COMPLETED          // 已完成
}

/**
 * 学习服务 - 管理学习会话和单词练习逻辑
 */
class StudyService(
    private val wordDao: WordDao,
    private val studySessionDao: StudySessionDao
) {

    private val _currentStudyWords = MutableStateFlow<List<WordStudyState>>(emptyList())
    val currentStudyWords: StateFlow<List<WordStudyState>> = _currentStudyWords.asStateFlow()

    private val _currentWordIndex = MutableStateFlow(0)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private val _isStudySessionActive = MutableStateFlow(false)
    val isStudySessionActive: StateFlow<Boolean> = _isStudySessionActive.asStateFlow()

    private val _currentPhase = MutableStateFlow(StudyPhase.MAIN_STUDY)
    val currentPhase: StateFlow<StudyPhase> = _currentPhase.asStateFlow()

    private var currentSessionId: Long? = null
    private val wrongWords = mutableListOf<WordStudyState>()

    // 短语"不认识"队列（用于重复练习）
    private val unknownPhrases = mutableListOf<WordStudyState>()
    private val reviewedPhraseIds = mutableSetOf<Long>() // 已经选择"认识"的短语ID

    /**
     * 开始学习会话
     */
    suspend fun startStudySession(deckId: Long, plannedCount: Int): Result<List<WordStudyState>> {
        return try {
            // 创建学习会话记录
            val session = StudySessionEntity(
                deckId = deckId,
                plannedCount = plannedCount
            )
            currentSessionId = studySessionDao.insertSession(session)

            // 获取要学习的单词
            val wordsToStudy = selectWordsForStudy(deckId, plannedCount)

            // 安全检查：确保列表不为空
            if (wordsToStudy.isEmpty()) {
                return Result.failure(IllegalStateException("No words available for study"))
            }

            val studyStates = wordsToStudy.map { word ->
                WordStudyState(word = word.toWord())
            }

            _currentStudyWords.value = studyStates
            _currentWordIndex.value = 0
            _isStudySessionActive.value = true
            _currentPhase.value = StudyPhase.MAIN_STUDY
            wrongWords.clear()
            unknownPhrases.clear()
            reviewedPhraseIds.clear()

            Result.success(studyStates)
        } catch (e: Exception) {
            AppLogger.e("Failed to start study session", e)
            Result.failure(e)
        }
    }

    /**
     * 选择要学习的单词
     * 优先选择需要复习的单词，然后是随机新单词
     */
    private suspend fun selectWordsForStudy(deckId: Long, count: Int): List<WordEntity> {
        val currentTime = System.currentTimeMillis()

        // 获取需要复习的单词
        val reviewWords = wordDao.getWordsForReview(currentTime)
            .filter { it.deckId == deckId }
            .take(count)

        val remainingCount = count - reviewWords.size

        return if (remainingCount > 0) {
            // 如果复习单词不够，添加随机新单词
            val reviewWordIds = reviewWords.map { it.id }.toSet()

            // 获取更多候选单词以确保去重后仍有足够数量
            val allRandomWords = wordDao.getRandomWordsFromDeck(deckId, remainingCount * 2)
            val randomWords = allRandomWords
                .filter { it.id !in reviewWordIds }  // 去重：排除已在复习列表中的单词
                .take(remainingCount)

            (reviewWords + randomWords).take(count)
        } else {
            reviewWords.take(count)
        }
    }

    /**
     * 获取当前单词的选择题选项
     */
    suspend fun getCurrentWordChoices(): List<ChoiceOption> {
        val currentWords = _currentStudyWords.value
        val currentIndex = _currentWordIndex.value

        if (currentIndex >= currentWords.size) return emptyList()

        val currentWordState = currentWords[currentIndex]

        // 短语类型不需要选择题选项
        if (currentWordState.questionType == com.example.english.data.model.WordQuestionType.PHRASE_RECOGNITION) {
            return emptyList()
        }

        return generateChoices(currentWordState.word)
    }

    /**
     * 生成选择题选项（1个正确答案 + 3个干扰项）
     */
    private suspend fun generateChoices(correctWord: Word): List<ChoiceOption> {
        val choices = mutableListOf<ChoiceOption>()

        // 添加正确答案
        choices.add(
            ChoiceOption(
                text = correctWord.chinese,
                phonetic = correctWord.phonetic,
                partOfSpeech = correctWord.partOfSpeech,
                isCorrect = true
            )
        )

        // 策略1: 优先从当前词库获取干扰项（排除当前单词）
        val sameDecDistractors = wordDao.getRandomWordsFromSameDeck(
            deckId = correctWord.deckId,
            excludeWordId = correctWord.id,
            count = 3
        )

        sameDecDistractors.forEach { distractor ->
            choices.add(
                ChoiceOption(
                    text = distractor.chinese,
                    phonetic = distractor.phonetic,
                    partOfSpeech = distractor.partOfSpeech,
                    isCorrect = false
                )
            )
        }

        // 策略2: 如果当前词库不够，从其他词库获取
        if (choices.size < 4) {
            val needed = 4 - choices.size
            val otherDeckDistractors = wordDao.getRandomWordsForDistractors(correctWord.deckId, needed)

            otherDeckDistractors.forEach { distractor ->
                choices.add(
                    ChoiceOption(
                        text = distractor.chinese,
                        phonetic = distractor.phonetic,
                        partOfSpeech = distractor.partOfSpeech,
                        isCorrect = false
                    )
                )
            }
        }

        // 策略3: 如果还不够，使用固定选项池
        if (choices.size < 4) {
            val needed = 4 - choices.size
            val defaultOptions = DefaultChoicesPool.getRandomDefaults(needed, correctWord.chinese)
            choices.addAll(defaultOptions)
        }

        // 随机打乱选项顺序
        return choices.shuffled()
    }

    /**
     * 处理用户选择
     */
    suspend fun handleUserChoice(selectedOption: ChoiceOption): Boolean {
        val currentWords = _currentStudyWords.value.toMutableList()
        val currentIndex = _currentWordIndex.value

        if (currentIndex >= currentWords.size) return false

        val currentWordState = currentWords[currentIndex]
        val isCorrect = selectedOption.isCorrect
        val newAttemptCount = currentWordState.attemptCount + 1

        // 更新单词状态
        val updatedWordState = currentWordState.copy(
            isAnswered = true,
            isCorrect = isCorrect,
            attemptCount = newAttemptCount
        )
        currentWords[currentIndex] = updatedWordState
        _currentStudyWords.value = currentWords

        // 如果回答错误，添加到错误列表
        if (!isCorrect) {
            wrongWords.add(updatedWordState)
        }

        // 更新数据库中的单词统计
        updateWordStatistics(currentWordState.word, isCorrect, newAttemptCount)

        // 更新学习会话进度
        updateSessionProgress()

        return isCorrect
    }

    /**
     * 处理短语答案（认识/不认识）
     * @param userKnows true=认识, false=不认识
     */
    suspend fun handlePhraseAnswer(userKnows: Boolean): Boolean {
        val currentWords = _currentStudyWords.value.toMutableList()
        val currentIndex = _currentWordIndex.value

        if (currentIndex >= currentWords.size) return false

        val currentWordState = currentWords[currentIndex]
        val newAttemptCount = currentWordState.attemptCount + 1

        // 更新单词状态
        val updatedWordState = currentWordState.copy(
            isAnswered = true,
            isCorrect = userKnows,
            attemptCount = newAttemptCount
        )
        currentWords[currentIndex] = updatedWordState
        _currentStudyWords.value = currentWords

        if (userKnows) {
            // 选择"认识"，记录已复习
            reviewedPhraseIds.add(currentWordState.word.id)
        } else {
            // 选择"不认识"，加入重复队列（如果还不在队列中）
            if (!unknownPhrases.any { it.word.id == currentWordState.word.id }) {
                unknownPhrases.add(updatedWordState.copy(
                    isAnswered = false,
                    isCorrect = false,
                    attemptCount = 0
                ))
            }
        }

        // 更新数据库中的单词统计
        updateWordStatistics(currentWordState.word, userKnows, newAttemptCount)

        // 更新学习会话进度
        updateSessionProgress()

        return userKnows
    }

    /**
     * 移动到下一个单词
     */
    suspend fun moveToNextWord() {
        val currentWords = _currentStudyWords.value
        val currentIndex = _currentWordIndex.value

        // 边界检查：如果已经超出范围，直接结束
        if (currentIndex >= currentWords.size) {
            finishStudySession()
            return
        }

        // 安全检查：确保当前单词已经回答
        if (currentIndex < currentWords.size && !currentWords[currentIndex].isAnswered) {
            // 当前单词还没回答，不应该移动
            return
        }

        if (currentIndex < currentWords.size - 1) {
            _currentWordIndex.value = currentIndex + 1
        } else {
            // 所有单词完成，优先处理短语"不认识"队列
            if (unknownPhrases.isNotEmpty()) {
                startReviewUnknownPhrases()
            } else if (wrongWords.isNotEmpty()) {
                // 如果没有短语队列，处理错误单词
                startReviewWrongWords()
            } else {
                // 都没有，结束学习会话
                finishStudySession()
            }
        }
    }

    /**
     * 移动到上一个单词
     */
    fun moveToPreviousWord() {
        val currentIndex = _currentWordIndex.value
        if (currentIndex > 0) {
            _currentWordIndex.value = currentIndex - 1
        }
    }

    /**
     * 开始复习"不认识"的短语
     */
    private suspend fun startReviewUnknownPhrases() {
        if (unknownPhrases.isNotEmpty()) {
            // 重置短语状态
            val resetPhrases = unknownPhrases.map { phraseState ->
                phraseState.copy(
                    isAnswered = false,
                    isCorrect = false,
                    attemptCount = 0
                )
            }
            _currentStudyWords.value = resetPhrases
            _currentWordIndex.value = 0
            _currentPhase.value = StudyPhase.PHRASE_REVIEW
            unknownPhrases.clear()
        } else if (wrongWords.isNotEmpty()) {
            // 短语复习完，处理错误单词
            startReviewWrongWords()
        } else {
            // 都完成了，结束会话
            finishStudySession()
        }
    }

    /**
     * 开始复习错误单词
     */
    private suspend fun startReviewWrongWords() {
        if (wrongWords.isEmpty()) {
            // 没有错误单词，直接结束学习会话
            finishStudySession()
            return
        }

        // 重置错误单词的状态
        val resetWrongWords = wrongWords.map { wordState ->
            wordState.copy(
                isAnswered = false,
                isCorrect = false,
                attemptCount = 0
            )
        }
        _currentStudyWords.value = resetWrongWords
        _currentWordIndex.value = 0
        _currentPhase.value = StudyPhase.WRONG_WORD_REVIEW
        wrongWords.clear()
    }

    /**
     * 获取当前学习进度
     */
    fun getStudyProgress(): Pair<Int, Int> {
        val currentIndex = _currentWordIndex.value
        val totalWords = _currentStudyWords.value.size
        return Pair(currentIndex + 1, totalWords)
    }

    /**
     * 结束学习会话
     */
    suspend fun finishStudySession() {
        currentSessionId?.let { sessionId ->
            studySessionDao.completeSession(sessionId, System.currentTimeMillis())
        }

        _isStudySessionActive.value = false
        _currentStudyWords.value = emptyList()
        _currentWordIndex.value = 0
        _currentPhase.value = StudyPhase.COMPLETED
        currentSessionId = null
        wrongWords.clear()
        unknownPhrases.clear()
        reviewedPhraseIds.clear()
    }

    /**
     * 更新单词统计信息
     */
    private suspend fun updateWordStatistics(word: Word, isCorrect: Boolean, attemptCount: Int) {
        val wordEntity = wordDao.getWordById(word.id) ?: return
        val currentTime = System.currentTimeMillis()

        // 如果是首次学习（firstLearnDate为0），初始化复习信息
        if (wordEntity.firstLearnDate == 0L) {
            val (reviewStage, nextReviewTime) = ReviewManager.initializeReview()
            wordDao.updateWordFirstLearn(
                wordId = word.id,
                learnDate = currentTime,
                stage = reviewStage,
                nextReviewTime = nextReviewTime,
                lastReviewTime = currentTime
            )
        } else {
            // 已经学习过，根据本次结果更新复习计划
            val (newStage, newNextReviewTime) = if (isCorrect) {
                ReviewManager.updateReviewRemembered(wordEntity.firstLearnDate, wordEntity.reviewStage)
            } else {
                ReviewManager.updateReviewForgotten(wordEntity.firstLearnDate)
            }

            if (isCorrect) {
                wordDao.updateWordReviewRemembered(
                    wordId = word.id,
                    stage = newStage,
                    nextReviewTime = newNextReviewTime,
                    lastReviewTime = currentTime
                )
            } else {
                wordDao.updateWordReviewForgotten(
                    wordId = word.id,
                    stage = newStage,
                    nextReviewTime = newNextReviewTime,
                    lastReviewTime = currentTime
                )
            }
        }
    }

    /**
     * 更新学习会话进度
     */
    private suspend fun updateSessionProgress() {
        currentSessionId?.let { sessionId ->
            val completedCount = _currentStudyWords.value.count { it.isAnswered }
            val correctCount = _currentStudyWords.value.count { it.isAnswered && it.isCorrect }

            studySessionDao.updateSessionProgress(sessionId, completedCount, correctCount)
        }
    }
}
