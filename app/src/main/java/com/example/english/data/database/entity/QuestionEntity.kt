package com.example.english.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.english.data.model.Question
import com.example.english.data.model.QuestionType

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["deck_id"])
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "deck_id")
    val deckId: Long,

    @ColumnInfo(name = "question_type")
    val questionType: String,  // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE

    @ColumnInfo(name = "question_text")
    val questionText: String,

    @ColumnInfo(name = "option_a")
    val optionA: String? = null,

    @ColumnInfo(name = "option_b")
    val optionB: String? = null,

    @ColumnInfo(name = "option_c")
    val optionC: String? = null,

    @ColumnInfo(name = "option_d")
    val optionD: String? = null,

    @ColumnInfo(name = "correct_answer")
    val correctAnswer: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    // SRS相关字段
    @ColumnInfo(name = "correct_count")
    val correctCount: Int = 0,

    @ColumnInfo(name = "wrong_count")
    val wrongCount: Int = 0,

    @ColumnInfo(name = "last_review_time")
    val lastReviewTime: Long = 0,

    @ColumnInfo(name = "next_review_time")
    val nextReviewTime: Long = 0,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Float = 2.5f,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 0,

    @ColumnInfo(name = "repetition")
    val repetition: Int = 0,

    @ColumnInfo(name = "first_learn_date")
    val firstLearnDate: Long = 0,

    @ColumnInfo(name = "review_stage")
    val reviewStage: Int = 0
)

fun QuestionEntity.toQuestion(): Question {
    return Question(
        id = id,
        deckId = deckId,
        questionType = QuestionType.valueOf(questionType),
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

fun Question.toEntity(): QuestionEntity {
    return QuestionEntity(
        id = id,
        deckId = deckId,
        questionType = questionType.name,
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

