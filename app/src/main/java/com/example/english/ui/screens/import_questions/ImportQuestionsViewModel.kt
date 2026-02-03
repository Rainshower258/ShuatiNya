package com.example.english.ui.screens.import_questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Deck
import com.example.english.data.model.ImportResult
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportQuestionsViewModel(
    private val deckRepository: DeckRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun loadDeck(deckId: Long) {
        viewModelScope.launch {
            try {
                val deck = deckRepository.getDeckById(deckId)
                _deck.value = deck
            } catch (e: Exception) {
                com.example.english.util.AppLogger.e("Error loading deck for question import: $deckId", e)
            }
        }
    }

    fun importQuestions(text: String, deckId: Long) {
        viewModelScope.launch {
            _isImporting.value = true
            _importResult.value = null
            try {
                val result = questionRepository.importQuestionsFromText(text, deckId)
                _importResult.value = result

                // 更新题库的题目数量
                if (result.successCount > 0) {
                    updateDeckQuestionCount(deckId)
                }
            } catch (e: Exception) {
                _importResult.value = ImportResult(
                    successCount = 0,
                    failureCount = 1,
                    errors = listOf("导入失败: ${e.message}")
                )
            } finally {
                _isImporting.value = false
            }
        }
    }

    private suspend fun updateDeckQuestionCount(deckId: Long) {
        try {
            val stats = questionRepository.getQuestionStats(deckId)
            val deck = _deck.value
            if (deck != null) {
                val updatedDeck = deck.copy(wordCount = stats.totalQuestions)
                deckRepository.updateDeck(updatedDeck)
                _deck.value = updatedDeck
            }
        } catch (e: Exception) {
            // 忽略错误
        }
    }
}

class ImportQuestionsViewModelFactory(
    private val deckRepository: DeckRepository,
    private val questionRepository: QuestionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportQuestionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportQuestionsViewModel(deckRepository, questionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

