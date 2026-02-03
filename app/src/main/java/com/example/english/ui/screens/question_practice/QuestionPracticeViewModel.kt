package com.example.english.ui.screens.question_practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.QuestionStudyState
import com.example.english.data.service.QuestionPracticeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionPracticeViewModel(
    private val practiceService: QuestionPracticeService
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuestionStudyState>>(emptyList())
    val questions: StateFlow<List<QuestionStudyState>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    fun startPractice(deckId: Long, plannedCount: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = practiceService.startPracticeSession(deckId, plannedCount)
                result.onSuccess { states ->
                    _questions.value = states
                    _currentIndex.value = 0
                    _isCompleted.value = false
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitAnswer(questionId: Long, answer: String) {
        viewModelScope.launch {
            practiceService.submitAnswer(questionId, answer)
            // 更新UI中的问题列表
            _questions.value = practiceService.currentQuestions.value
        }
    }

    fun moveToNext() {
        val nextIndex = _currentIndex.value + 1
        if (nextIndex < _questions.value.size) {
            _currentIndex.value = nextIndex
            practiceService.moveToNextQuestion()
        } else {
            _isCompleted.value = true
            practiceService.completePracticeSession()
        }
    }

    fun moveToPrevious() {
        val prevIndex = _currentIndex.value - 1
        if (prevIndex >= 0) {
            _currentIndex.value = prevIndex
            practiceService.moveToPreviousQuestion()
        }
    }
}

class QuestionPracticeViewModelFactory(
    private val practiceService: QuestionPracticeService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionPracticeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionPracticeViewModel(practiceService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

