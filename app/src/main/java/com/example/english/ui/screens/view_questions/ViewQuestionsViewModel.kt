package com.example.english.ui.screens.view_questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Question
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.QuestionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ViewQuestionsUiState(
    val deckName: String = "",
    val allQuestions: List<Question> = emptyList(),
    val filteredQuestions: List<Question> = emptyList(),
    val searchKeyword: String = "",
    val selectedQuestions: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ViewQuestionsViewModel(
    private val questionRepository: QuestionRepository,
    private val deckRepository: DeckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewQuestionsUiState())
    val uiState: StateFlow<ViewQuestionsUiState> = _uiState.asStateFlow()

    private var currentDeckId: Long = 0

    fun loadQuestions(deckId: Long) {
        currentDeckId = deckId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 加载词库名称
            deckRepository.getDeckById(deckId)?.let { deck ->
                _uiState.update { it.copy(deckName = deck.name) }
            }

            // 加载所有题目
            questionRepository.getQuestionsByDeckId(deckId).collect { questions ->
                _uiState.update {
                    it.copy(
                        allQuestions = questions,
                        filteredQuestions = questions,
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
                // 如果搜索关键字为空，显示所有题目
                _uiState.update { it.copy(filteredQuestions = it.allQuestions) }
            } else {
                // 执行搜索
                val results = questionRepository.searchQuestions(currentDeckId, keyword)
                _uiState.update { it.copy(filteredQuestions = results) }
            }
        }
    }

    fun toggleQuestionSelection(questionId: Long) {
        _uiState.update {
            val newSelection = if (questionId in it.selectedQuestions) {
                it.selectedQuestions - questionId
            } else {
                it.selectedQuestions + questionId
            }
            it.copy(selectedQuestions = newSelection)
        }
    }

    fun selectAllQuestions() {
        _uiState.update {
            it.copy(selectedQuestions = it.filteredQuestions.map { question -> question.id }.toSet())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedQuestions = emptySet()) }
    }

    fun deleteSelectedQuestions() {
        viewModelScope.launch {
            try {
                val selectedQuestionIds = _uiState.value.selectedQuestions
                val questionsToDelete = _uiState.value.allQuestions.filter { it.id in selectedQuestionIds }

                questionRepository.deleteQuestions(questionsToDelete)

                _uiState.update {
                    it.copy(
                        selectedQuestions = emptySet(),
                        successMessage = "已删除 ${questionsToDelete.size} 个题目"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "删除失败: ${e.message}")
                }
            }
        }
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch {
            try {
                questionRepository.updateQuestion(question)
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

class ViewQuestionsViewModelFactory(
    private val questionRepository: QuestionRepository,
    private val deckRepository: DeckRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewQuestionsViewModel::class.java)) {
            return ViewQuestionsViewModel(questionRepository, deckRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

