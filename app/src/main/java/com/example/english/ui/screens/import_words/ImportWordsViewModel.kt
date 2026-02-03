package com.example.english.ui.screens.import_words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Deck
import com.example.english.data.model.ImportResult
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportWordsViewModel(
    private val deckRepository: DeckRepository,
    private val wordRepository: WordRepository
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
                com.example.english.util.AppLogger.e("Error loading deck for import: $deckId", e)
            }
        }
    }

    fun importWordsFromText(text: String, deckId: Long) {
        viewModelScope.launch {
            _isImporting.value = true
            _importResult.value = null

            try {
                val result = wordRepository.importWordsFromText(text, deckId)
                _importResult.value = result

                // 如果导入成功，更新词库的单词数量
                if (result.successCount > 0) {
                    deckRepository.updateDeckWordCount(deckId)
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
}

class ImportWordsViewModelFactory(
    private val deckRepository: DeckRepository,
    private val wordRepository: WordRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportWordsViewModel::class.java)) {
            return ImportWordsViewModel(deckRepository, wordRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
