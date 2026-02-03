package com.example.english.data.database.dao

import androidx.room.*
import com.example.english.data.database.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks WHERE is_active = 1 ORDER BY updated_at DESC")
    fun getAllActiveDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks ORDER BY updated_at DESC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Long): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("UPDATE decks SET is_active = 0 WHERE id = :deckId")
    suspend fun deactivateDeck(deckId: Long)

    @Query("UPDATE decks SET word_count = :count, updated_at = :updatedAt WHERE id = :deckId")
    suspend fun updateWordCount(deckId: Long, count: Int, updatedAt: Long)
}

