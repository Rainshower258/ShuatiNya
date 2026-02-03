package com.example.english.data.model

/**
 * 复习记录 - 按日期分组的学习和复习单词
 */
data class ReviewRecord(
    val date: String,                      // 日期 "2025-01-15"
    val dateTimestamp: Long,               // 日期时间戳
    val wordsLearned: List<Word>,          // 当天学习的单词
    val wordsToReview: List<Word>,         // 当天需要复习的单词
    val reviewedWords: List<Word>          // 当天已复习的单词
) {
    val totalWordsLearned: Int
        get() = wordsLearned.size

    val totalWordsToReview: Int
        get() = wordsToReview.size

    val totalWordsReviewed: Int
        get() = reviewedWords.size
}

/**
 * 复习统计
 */
data class ReviewStatistics(
    val totalLearned: Int,                 // 总学习单词数
    val totalToReview: Int,                // 待复习单词数
    val todayLearned: Int,                 // 今日学习数
    val todayReviewed: Int,                // 今日复习数
    val continuousDays: Int,               // 连续学习天数
    val masteredWords: Int                 // 已掌握单词数（stage > 5）
)

/**
 * 复习单词状态（用于复习界面）
 */
data class ReviewWordState(
    val word: Word,
    val reviewStage: Int,                  // 复习阶段
    val nextReviewDate: String,            // 下次复习日期
    val isRemembered: Boolean? = null,     // 本次复习是否记住（null=未答题）
    val reviewCount: Int = 0               // 已复习次数
)

/**
 * 词库复习信息 - 用于复习中心按词库分组显示
 */
data class DeckReviewInfo(
    val deckId: Long,                      // 词库ID
    val deckName: String,                  // 词库名称
    val totalWords: Int,                   // 总词数
    val learnedWords: Int,                 // 已学习词数
    val reviewWords: Int,                  // 待复习词数
    val masteredWords: Int,                // 已掌握词数
    val lastStudyDate: Long?,              // 最后学习时间
    val todayLearned: Int = 0,             // 今日学习数
    val todayReviewed: Int = 0             // 今日复习数
)

/**
 * 词库学习记录 - 用于显示某个词库的学习历史
 */
data class DeckStudyRecord(
    val sessionId: Long,
    val deckId: Long,
    val deckName: String,
    val date: String,                      // 格式化的日期
    val timestamp: Long,                   // 时间戳
    val plannedCount: Int,                 // 计划学习数
    val completedCount: Int,               // 完成数
    val correctCount: Int,                 // 正确数
    val accuracy: Float                    // 正确率
) {
    val accuracyPercent: Int
        get() = (accuracy * 100).toInt()
}

