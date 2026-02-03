package com.example.english.data.model

/**
 * 词库类型枚举
 */
enum class DeckType {
    VOCABULARY,  // 背单词模式
    QUESTION     // 刷题模式
}

/**
 * 词库数据模型
 */
data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val deckType: DeckType = DeckType.VOCABULARY  // 词库类型
)

/**
 * 学习统计
 */
data class StudyStats(
    val deckId: Long,
    val totalWords: Int,
    val studiedWords: Int,
    val masteredWords: Int,
    val todayStudiedCount: Int,
    val studyStreak: Int // 连续学习天数
)

/**
 * 词库导入结果
 */
data class ImportResult(
    val successCount: Int,
    val failureCount: Int,
    val duplicateCount: Int = 0,
    val errors: List<String> = emptyList()
)

