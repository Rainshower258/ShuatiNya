package com.example.english.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.database.dao.WordDao
import com.example.english.data.database.entity.toWord
import com.example.english.data.model.ReviewRecord
import com.example.english.data.model.ReviewStatistics
import com.example.english.data.model.Word
import com.example.english.data.service.ReviewManager
import com.example.english.util.DateTimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 复习功能ViewModel
 */
class ReviewViewModel(
    private val wordDao: WordDao
) : ViewModel() {

    private val _reviewRecords = MutableStateFlow<List<ReviewRecord>>(emptyList())
    val reviewRecords: StateFlow<List<ReviewRecord>> = _reviewRecords.asStateFlow()

    private val _statistics = MutableStateFlow<ReviewStatistics?>(null)
    val statistics: StateFlow<ReviewStatistics?> = _statistics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadReviewData()
    }

    /**
     * 加载复习数据
     */
    fun loadReviewData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载统计信息
                loadStatistics()

                // 加载复习记录（按日期分组）
                loadReviewRecords()
            } catch (e: Exception) {
                com.example.english.util.AppLogger.e("Error loading review data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载统计信息
     */
    private suspend fun loadStatistics() {
        val currentTime = DateTimeHelper.getCurrentTimeMillis()
        val todayStart = DateTimeHelper.getTodayStart()
        val todayEnd = DateTimeHelper.getTodayEnd()

        // 获取所有学习中的单词
        val allLearningWords = wordDao.getAllLearningWords()

        // 获取今日需要复习的单词
        val todayReviewWords = wordDao.getWordsNeedReview(currentTime)

        // 获取今日学习的单词
        val todayLearnedWords = wordDao.getWordsLearnedOnDate(todayStart, todayEnd)

        // 计算已掌握的单词（stage > 5）
        val masteredWords = allLearningWords.count { it.reviewStage > 5 }

        // 计算连续学习天数（简化版，后续可优化）
        val continuousDays = calculateContinuousDays()

        _statistics.value = ReviewStatistics(
            totalLearned = allLearningWords.size,
            totalToReview = todayReviewWords.size,
            todayLearned = todayLearnedWords.size,
            todayReviewed = 0, // 需要从会话记录中统计
            continuousDays = continuousDays,
            masteredWords = masteredWords
        )
    }

    /**
     * 加载复习记录（按日期分组）
     */
    private suspend fun loadReviewRecords() {
        val allLearnedWords = wordDao.getAllLearnedWordsOrderByDate()
        val currentTime = DateTimeHelper.getCurrentTimeMillis()

        // 按日期分组
        val recordsMap = mutableMapOf<String, MutableList<Word>>()

        allLearnedWords.forEach { wordEntity ->
            val dateStr = DateTimeHelper.formatDate(wordEntity.firstLearnDate)
            val word = wordEntity.toWord()

            if (!recordsMap.containsKey(dateStr)) {
                recordsMap[dateStr] = mutableListOf()
            }
            recordsMap[dateStr]?.add(word)
        }

        // 获取今日需要复习的单词
        val todayReviewWords = wordDao.getWordsNeedReview(currentTime).map { it.toWord() }

        // 构建复习记录列表
        val records = mutableListOf<ReviewRecord>()

        // 添加今日记录
        val todayDate = DateTimeHelper.getCurrentDateString()
        val todayStart = DateTimeHelper.getTodayStart()
        val todayEnd = DateTimeHelper.getTodayEnd()
        val todayLearned = wordDao.getWordsLearnedOnDate(todayStart, todayEnd).map { it.toWord() }

        records.add(
            ReviewRecord(
                date = todayDate,
                dateTimestamp = todayStart,
                wordsLearned = todayLearned,
                wordsToReview = todayReviewWords,
                reviewedWords = emptyList() // 后续从会话记录中获取
            )
        )

        // 添加历史记录
        recordsMap.entries
            .filter { it.key != todayDate }
            .sortedByDescending { it.key }
            .forEach { (dateStr, words) ->
                val timestamp = words.firstOrNull()?.createdAt ?: 0L
                records.add(
                    ReviewRecord(
                        date = dateStr,
                        dateTimestamp = timestamp,
                        wordsLearned = words,
                        wordsToReview = emptyList(),
                        reviewedWords = emptyList()
                    )
                )
            }

        _reviewRecords.value = records
    }

    /**
     * 计算连续学习天数
     */
    private suspend fun calculateContinuousDays(): Int {
        val allLearnedWords = wordDao.getAllLearnedWordsOrderByDate()
        if (allLearnedWords.isEmpty()) return 0

        val today = DateTimeHelper.getTodayStart()
        var continuousDays = 0
        var checkDate = today

        // 从今天往前检查，最多检查30天
        for (i in 0..30) {
            val dayStart = DateTimeHelper.getStartOfDay(checkDate)
            val dayEnd = DateTimeHelper.getEndOfDay(checkDate)

            val hasLearned = allLearnedWords.any { word ->
                word.firstLearnDate in dayStart..dayEnd
            }

            if (hasLearned) {
                continuousDays++
                checkDate = DateTimeHelper.addDays(checkDate, -1)
            } else if (i > 0) {
                // 如果不是第一天且今天没学习，中断连续
                break
            } else {
                // 今天还没学习，继续检查昨天
                checkDate = DateTimeHelper.addDays(checkDate, -1)
            }
        }

        return continuousDays
    }

    /**
     * 获取今日需要复习的单词
     */
    suspend fun getTodayReviewWords(): List<Word> {
        val currentTime = DateTimeHelper.getCurrentTimeMillis()
        return wordDao.getWordsNeedReview(currentTime).map { it.toWord() }
    }

    /**
     * 获取指定日期学习的单词
     */
    suspend fun getWordsLearnedOnDate(dateStr: String): List<Word> {
        // 解析日期字符串，获取该日期的开始和结束时间戳
        // 简化处理：使用当前记录中的数据
        return _reviewRecords.value
            .find { it.date == dateStr }
            ?.wordsLearned
            ?: emptyList()
    }
}

/**
 * ReviewViewModel工厂
 */
class ReviewViewModelFactory(
    private val wordDao: WordDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(wordDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

