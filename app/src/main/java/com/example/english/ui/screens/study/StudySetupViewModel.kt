/*
 * MIT License
 * Copyright (c) 2025 sun6 (Rainshower258)
 * See LICENSE file in the project root for full license information.
 */
package com.example.english.ui.screens.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Deck
import com.example.english.data.repository.DeckRepository
import com.example.english.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudySetupViewModel(
    private val deckRepository: DeckRepository
) : ViewModel() {

    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck.asStateFlow()

    private val _wordCount = MutableStateFlow(0)
    val wordCount: StateFlow<Int> = _wordCount.asStateFlow()

    fun loadDeck(deckId: Long) {
        viewModelScope.launch {
            try {
                val deck = deckRepository.getDeckById(deckId)
                _deck.value = deck

                if (deck != null) {
                    val stats = deckRepository.getDeckStudyStats(deckId)
                    _wordCount.value = stats.totalWords
                }
            } catch (e: Exception) {
                AppLogger.e("Error loading deck: $deckId", e)
            }
        }
    }
}

class StudySetupViewModelFactory(
    private val deckRepository: DeckRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudySetupViewModel::class.java)) {
            return StudySetupViewModel(deckRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
