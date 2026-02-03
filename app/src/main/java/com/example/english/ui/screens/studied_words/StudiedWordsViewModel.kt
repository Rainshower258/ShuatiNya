package com.example.english.ui.screens.studied_words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Deck
import com.example.english.data.model.Word
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import com.example.english.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudiedWordsViewModel(
    private val deckRepository: DeckRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck.asStateFlow()

    private val _studiedWords = MutableStateFlow<List<Word>>(emptyList())
    val studiedWords: StateFlow<List<Word>> = _studiedWords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadStudiedWords(deckId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载词库信息
                val deck = deckRepository.getDeckById(deckId)
                _deck.value = deck

                // 加载已学单词
                val words = wordRepository.getStudiedWords(deckId)
                _studiedWords.value = words
            } catch (e: Exception) {
                AppLogger.e("Error loading studied words for deck: $deckId", e)
                _studiedWords.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class StudiedWordsViewModelFactory(
    private val deckRepository: DeckRepository,
    private val wordRepository: WordRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudiedWordsViewModel::class.java)) {
            return StudiedWordsViewModel(deckRepository, wordRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
