package com.example.english.data.model

/**
 * 题目类型枚举
 */
enum class QuestionType {
    SINGLE_CHOICE,   // 单选题
    MULTIPLE_CHOICE, // 多选题
    TRUE_FALSE       // 判断题
}

/**
 * 题目数据模型
 */
data class Question(
    val id: Long = 0,
    val deckId: Long,
    val questionType: QuestionType,
    val questionText: String,         // 题目内容
    val optionA: String? = null,      // 选项A
    val optionB: String? = null,      // 选项B
    val optionC: String? = null,      // 选项C
    val optionD: String? = null,      // 选项D
    val correctAnswer: String,        // 正确答案（如 "A"、"AB"、"true"）
    val createdAt: Long = System.currentTimeMillis(),

    // SRS相关字段
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewTime: Long = 0L,
    val nextReviewTime: Long = 0L,
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetition: Int = 0,
    val firstLearnDate: Long = 0L,
    val reviewStage: Int = 0
)

/**
 * 题目学习状态
 */
data class QuestionStudyState(
    val question: Question,
    var isCorrect: Boolean? = null,
    var userAnswer: String? = null,
    var isCompleted: Boolean = false
)

/**
 * 题目选项
 */
data class QuestionOption(
    val id: String,              // A, B, C, D
    val text: String,
    val isSelected: Boolean = false
)

