package com.example.english.ui.screens.question_practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Deck
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionSetupViewModel(
    private val deckRepository: DeckRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {
    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck.asStateFlow()

    private val _questionCount = MutableStateFlow(0)
    val questionCount: StateFlow<Int> = _questionCount.asStateFlow()

    fun loadDeck(deckId: Long) {
        viewModelScope.launch {
            try {
                val deck = deckRepository.getDeckById(deckId)
                _deck.value = deck
                if (deck != null) {
                    val stats = questionRepository.getQuestionStats(deckId)
                    _questionCount.value = stats.totalQuestions
                }
            } catch (e: Exception) {
                com.example.english.util.AppLogger.e("Error loading deck for question setup: $deckId", e)
            }
        }
    }
}

class QuestionSetupViewModelFactory(
    private val deckRepository: DeckRepository,
    private val questionRepository: QuestionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionSetupViewModel(deckRepository, questionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

