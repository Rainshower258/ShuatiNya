package com.example.english.ui.screens.view_words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Word
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ViewWordsUiState(
    val deckName: String = "",
    val allWords: List<Word> = emptyList(),
    val filteredWords: List<Word> = emptyList(),
    val searchKeyword: String = "",
    val selectedWords: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ViewWordsViewModel(
    private val wordRepository: WordRepository,
    private val deckRepository: DeckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewWordsUiState())
    val uiState: StateFlow<ViewWordsUiState> = _uiState.asStateFlow()

    private var currentDeckId: Long = 0

    fun loadWords(deckId: Long) {
        currentDeckId = deckId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 加载词库名称
            deckRepository.getDeckById(deckId)?.let { deck ->
                _uiState.update { it.copy(deckName = deck.name) }
            }

            // 加载所有单词
            wordRepository.getWordsByDeckId(deckId).collect { words ->
                _uiState.update {
                    it.copy(
                        allWords = words,
                        filteredWords = words,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchKeywordChange(keyword: String) {
        _uiState.update { it.copy(searchKeyword = keyword) }
        performSearch(keyword)
    }

    private fun performSearch(keyword: String) {
        viewModelScope.launch {
            if (keyword.isBlank()) {
                // 如果搜索关键字为空，显示所有单词
                _uiState.update { it.copy(filteredWords = it.allWords) }
            } else {
                // 执行搜索
                val results = wordRepository.searchWords(currentDeckId, keyword)
                _uiState.update { it.copy(filteredWords = results) }
            }
        }
    }

    fun toggleWordSelection(wordId: Long) {
        _uiState.update {
            val newSelection = if (wordId in it.selectedWords) {
                it.selectedWords - wordId
            } else {
                it.selectedWords + wordId
            }
            it.copy(selectedWords = newSelection)
        }
    }

    fun selectAllWords() {
        _uiState.update {
            it.copy(selectedWords = it.filteredWords.map { word -> word.id }.toSet())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedWords = emptySet()) }
    }

    fun deleteSelectedWords() {
        viewModelScope.launch {
            try {
                val selectedWordIds = _uiState.value.selectedWords
                val wordsToDelete = _uiState.value.allWords.filter { it.id in selectedWordIds }

                wordRepository.deleteWords(wordsToDelete)

                _uiState.update {
                    it.copy(
                        selectedWords = emptySet(),
                        successMessage = "已删除 ${wordsToDelete.size} 个单词"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "删除失败: ${e.message}")
                }
            }
        }
    }

    fun updateWord(word: Word) {
        viewModelScope.launch {
            try {
                wordRepository.updateWord(word)
                _uiState.update {
                    it.copy(successMessage = "更新成功")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新失败: ${e.message}")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

class ViewWordsViewModelFactory(
    private val wordRepository: WordRepository,
    private val deckRepository: DeckRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewWordsViewModel::class.java)) {
            return ViewWordsViewModel(wordRepository, deckRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

