package com.example.english.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.entity.toWord
import com.example.english.data.model.ChoiceOption
import com.example.english.data.model.ReviewWordState
import com.example.english.data.model.Word
import com.example.english.data.service.ReviewManager
import com.example.english.util.DateTimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 复习练习ViewModel
 */
class ReviewPracticeViewModel(
    private val wordDao: WordDao
) : ViewModel() {

    private val _reviewWords = MutableStateFlow<List<ReviewWordState>>(emptyList())
    val reviewWords: StateFlow<List<ReviewWordState>> = _reviewWords.asStateFlow()

    private val _currentWordIndex = MutableStateFlow(0)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private val _choices = MutableStateFlow<List<ChoiceOption>>(emptyList())
    val choices: StateFlow<List<ChoiceOption>> = _choices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showAnswer = MutableStateFlow(false)
    val showAnswer: StateFlow<Boolean> = _showAnswer.asStateFlow()

    /**
     * 开始复习
     */
    fun startReview() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentTime = DateTimeHelper.getCurrentTimeMillis()
                val wordsToReview = wordDao.getWordsNeedReview(currentTime)

                val reviewStates = wordsToReview.map { wordEntity ->
                    ReviewWordState(
                        word = wordEntity.toWord(),
                        reviewStage = wordEntity.reviewStage,
                        nextReviewDate = DateTimeHelper.formatDate(wordEntity.nextReviewTime),
                        reviewCount = wordEntity.correctCount + wordEntity.wrongCount
                    )
                }

                _reviewWords.value = reviewStates
                _currentWordIndex.value = 0
                loadCurrentWordChoices()
            } catch (e: Exception) {
                com.example.english.util.AppLogger.e("Error starting review", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载当前单词的选择题
     */
    private fun loadCurrentWordChoices() {
        viewModelScope.launch {
            val reviewWords = _reviewWords.value
            val currentIndex = _currentWordIndex.value

            if (currentIndex >= reviewWords.size) {
                _choices.value = emptyList()
                return@launch
            }

            val currentWord = reviewWords[currentIndex].word
            val choices = generateChoices(currentWord)
            _choices.value = choices
            _showAnswer.value = false
        }
    }

    /**
     * 生成选择题
     */
    private suspend fun generateChoices(correctWord: Word): List<ChoiceOption> {
        val choices = mutableListOf<ChoiceOption>()

        // 正确答案
        choices.add(
            ChoiceOption(
                text = correctWord.chinese,
                phonetic = "",
                partOfSpeech = correctWord.partOfSpeech,
                isCorrect = true
            )
        )

        // 获取干扰项
        val distractors = wordDao.getRandomWordsFromSameDeck(
            deckId = correctWord.deckId,
            excludeWordId = correctWord.id,
            count = 3
        )

        distractors.forEach { distractor ->
            choices.add(
                ChoiceOption(
                    text = distractor.chinese,
                    phonetic = "",
                    partOfSpeech = distractor.partOfSpeech,
                    isCorrect = false
                )
            )
        }

        // 如果不够3个干扰项，从其他词库获取
        if (choices.size < 4) {
            val needed = 4 - choices.size
            val otherDistractors = wordDao.getRandomWordsForDistractors(correctWord.deckId, needed)
            otherDistractors.forEach { distractor ->
                choices.add(
                    ChoiceOption(
                        text = distractor.chinese,
                        phonetic = "",
                        partOfSpeech = distractor.partOfSpeech,
                        isCorrect = false
                    )
                )
            }
        }

        return choices.shuffled()
    }

    /**
     * 处理用户选择
     */
    fun handleChoice(choice: ChoiceOption) {
        _showAnswer.value = true
        val isCorrect = choice.isCorrect

        // 更新当前单词状态
        updateCurrentWordState(isCorrect)
    }

    /**
     * 更新当前单词的复习状态
     */
    private fun updateCurrentWordState(isRemembered: Boolean) {
        viewModelScope.launch {
            val reviewWords = _reviewWords.value.toMutableList()
            val currentIndex = _currentWordIndex.value

            if (currentIndex >= reviewWords.size) return@launch

            val currentWordState = reviewWords[currentIndex]
            val updatedState = currentWordState.copy(isRemembered = isRemembered)
            reviewWords[currentIndex] = updatedState

            _reviewWords.value = reviewWords

            // 更新数据库
            updateWordReviewStatus(currentWordState.word, isRemembered)
        }
    }

    /**
     * 更新数据库中的复习状态
     */
    private suspend fun updateWordReviewStatus(word: Word, isRemembered: Boolean) {
        val wordEntity = wordDao.getWordById(word.id) ?: return
        val currentTime = DateTimeHelper.getCurrentTimeMillis()

        val (newStage, nextReviewTime) = if (isRemembered) {
            ReviewManager.updateReviewRemembered(wordEntity.firstLearnDate, wordEntity.reviewStage)
        } else {
            ReviewManager.updateReviewForgotten(wordEntity.firstLearnDate)
        }

        if (isRemembered) {
            wordDao.updateWordReviewRemembered(
                wordId = word.id,
                stage = newStage,
                nextReviewTime = nextReviewTime,
                lastReviewTime = currentTime
            )
        } else {
            wordDao.updateWordReviewForgotten(
                wordId = word.id,
                stage = newStage,
                nextReviewTime = nextReviewTime,
                lastReviewTime = currentTime
            )
        }
    }

    /**
     * 移动到下一个单词
     */
    fun moveToNextWord() {
        val currentIndex = _currentWordIndex.value
        val totalWords = _reviewWords.value.size

        if (currentIndex < totalWords - 1) {
            _currentWordIndex.value = currentIndex + 1
            loadCurrentWordChoices()
        }
    }

    /**
     * 移动到上一个单词
     */
    fun moveToPreviousWord() {
        val currentIndex = _currentWordIndex.value
        if (currentIndex > 0) {
            _currentWordIndex.value = currentIndex - 1
            loadCurrentWordChoices()
        }
    }

    /**
     * 获取进度
     */
    fun getProgress(): Pair<Int, Int> {
        return Pair(_currentWordIndex.value + 1, _reviewWords.value.size)
    }

    /**
     * 是否已完成
     */
    fun isCompleted(): Boolean {
        return _currentWordIndex.value >= _reviewWords.value.size - 1 && _showAnswer.value
    }
}

/**
 * Factory
 */
class ReviewPracticeViewModelFactory(
    private val wordDao: WordDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewPracticeViewModel::class.java)) {
            return ReviewPracticeViewModel(wordDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

