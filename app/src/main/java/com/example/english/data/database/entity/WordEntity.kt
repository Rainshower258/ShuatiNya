package com.example.english.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.english.data.model.Word

@Entity(
    tableName = "words",
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
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "english")
    val english: String,

    @ColumnInfo(name = "chinese")
    val chinese: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,

    @ColumnInfo(name = "phonetic")
    val phonetic: String,

    @ColumnInfo(name = "deck_id")
    val deckId: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    // SRS相关字段
    @ColumnInfo(name = "correct_count")
    val correctCount: Int = 0,

    @ColumnInfo(name = "wrong_count")
    val wrongCount: Int = 0,

    @ColumnInfo(name = "last_review_time")
    val lastReviewTime: Long = 0L,

    @ColumnInfo(name = "next_review_time")
    val nextReviewTime: Long = 0L,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Float = 2.5f,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 1,

    @ColumnInfo(name = "repetition")
    val repetition: Int = 0,

    // 复习功能新增字段
    @ColumnInfo(name = "first_learn_date")
    val firstLearnDate: Long = 0L, // 首次学习时间戳

    @ColumnInfo(name = "review_stage")
    val reviewStage: Int = 0, // 复习阶段 (0=未学, 1=1天后, 2=3天后, 3=7天后, 4=15天后, 5=30天后)

    // 短语支持字段
    @ColumnInfo(name = "word_type")
    val wordType: String = "WORD", // "WORD" 或 "PHRASE"

    @ColumnInfo(name = "phrase_usage")
    val phraseUsage: String? = null // 短语用法示例
)

fun WordEntity.toWord(): Word {
    return Word(
        id = id,
        english = english,
        chinese = chinese,
        partOfSpeech = partOfSpeech,
        phonetic = phonetic,
        deckId = deckId,
        createdAt = createdAt,
        wordType = com.example.english.data.model.WordType.valueOf(wordType),
        phraseUsage = phraseUsage,
        correctCount = correctCount,
        wrongCount = wrongCount,
        reviewStage = reviewStage
    )
}

fun Word.toEntity(): WordEntity {
    return WordEntity(
        id = id,
        english = english,
        chinese = chinese,
        partOfSpeech = partOfSpeech,
        phonetic = phonetic,
        deckId = deckId,
        createdAt = createdAt,
        wordType = wordType.name,
        phraseUsage = phraseUsage,
        correctCount = correctCount,
        wrongCount = wrongCount,
        reviewStage = reviewStage
    )
}

