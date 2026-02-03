package com.example.english.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.english.data.model.Deck

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "word_count")
    val wordCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "deck_type")
    val deckType: String = "VOCABULARY"  // VOCABULARY 或 QUESTION
)

fun DeckEntity.toDeck(): Deck {
    return Deck(
        id = id,
        name = name,
        description = description,
        wordCount = wordCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive,
        deckType = com.example.english.data.model.DeckType.valueOf(deckType)
    )
}

fun Deck.toEntity(): DeckEntity {
    return DeckEntity(
        id = id,
        name = name,
        description = description,
        wordCount = wordCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive,
        deckType = deckType.name
    )
}

