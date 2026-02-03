package com.example.english.ui.screens.practiced_questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Question
import com.example.english.data.repository.QuestionRepository
import com.example.english.data.repository.QuestionStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticedQuestionsViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _stats = MutableStateFlow(
        QuestionStats(
            deckId = 0,
            totalQuestions = 0,
            learnedQuestions = 0,
            reviewQuestions = 0,
            masteredQuestions = 0
        )
    )
    val stats: StateFlow<QuestionStats> = _stats.asStateFlow()

    fun loadQuestions(deckId: Long) {
        viewModelScope.launch {
            try {
                // 加载统计信息
                val stats = questionRepository.getQuestionStats(deckId)
                _stats.value = stats

                // 加载已练习的题目
                questionRepository.getQuestionsByDeckId(deckId).collect { questionList ->
                    _questions.value = questionList.filter { it.firstLearnDate > 0 }
                        .sortedByDescending { it.lastReviewTime }
                }
            } catch (e: Exception) {
                com.example.english.util.AppLogger.e("Error loading practiced questions for deck: $deckId", e)
            }
        }
    }
}

class PracticedQuestionsViewModelFactory(
    private val questionRepository: QuestionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticedQuestionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PracticedQuestionsViewModel(questionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

