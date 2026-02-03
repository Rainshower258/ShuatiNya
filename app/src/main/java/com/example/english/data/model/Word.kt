package com.example.english.data.model

/**
 * 单词类型枚举
 */
enum class WordType {
    WORD,    // 普通单词
    PHRASE   // 短语
}

/**
 * 单词数据模型
 */
data class Word(
    val id: Long = 0,
    val english: String,
    val chinese: String,
    val partOfSpeech: String, // 词性
    val phonetic: String, // 音标
    val deckId: Long,
    val createdAt: Long = System.currentTimeMillis(),

    // 短语支持
    val wordType: WordType = WordType.WORD,  // 单词/短语类型
    val phraseUsage: String? = null,          // 短语用法示例（可选）

    // 学习统计（用于显示）
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val reviewStage: Int = 0
)

/**
 * 单词学习记录
 */
data class WordLearningRecord(
    val wordId: Long,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewTime: Long = 0L,
    val nextReviewTime: Long = 0L,
    val easeFactor: Float = 2.5f, // SRS算法的难度因子
    val interval: Int = 1, // 复习间隔（天）
    val repetition: Int = 0 // 重复次数
)

/**
 * 选择题选项
 */
data class ChoiceOption(
    val text: String,
    val phonetic: String,
    val partOfSpeech: String,
    val isCorrect: Boolean
)

/**
 * 单词题目类型枚举
 */
enum class WordQuestionType {
    WORD_CHOICE,           // 单词选择题（4选1）
    PHRASE_RECOGNITION     // 短语认知判断（认识/不认识）
}

/**
 * 学习会话中的单词状态
 */
data class WordStudyState(
    val word: Word,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val attemptCount: Int = 0,
    val questionType: WordQuestionType = if (word.wordType == WordType.PHRASE)
        WordQuestionType.PHRASE_RECOGNITION
    else
        WordQuestionType.WORD_CHOICE
)
