package com.example.english.ui.screens.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.ChoiceOption
import com.example.english.data.model.WordStudyState
import com.example.english.data.repository.DeckRepository
import com.example.english.data.repository.WordRepository
import com.example.english.data.service.StudyService
import com.example.english.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(
    private val studyService: StudyService,
    private val deckRepository: DeckRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _studyWords = MutableStateFlow<List<WordStudyState>>(emptyList())
    val studyWords: StateFlow<List<WordStudyState>> = _studyWords.asStateFlow()

    private val _currentWordIndex = MutableStateFlow(0)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private val _choices = MutableStateFlow<List<ChoiceOption>>(emptyList())
    val choices: StateFlow<List<ChoiceOption>> = _choices.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 暴露学习阶段
    val currentPhase = studyService.currentPhase

    init {
        // 监听学习服务的状态变化
        viewModelScope.launch {
            studyService.currentStudyWords.collect { words ->
                _studyWords.value = words
            }
        }

        viewModelScope.launch {
            studyService.currentWordIndex.collect { index ->
                _currentWordIndex.value = index
                loadCurrentWordChoices()
            }
        }
    }

    fun startStudySession(deckId: Long, plannedCount: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = studyService.startStudySession(deckId, plannedCount)
                if (result.isSuccess) {
                    loadCurrentWordChoices()
                } else {
                    _errorMessage.value = "开始学习失败，请重试"
                    AppLogger.e("Failed to start study session: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                AppLogger.e("Error starting study session", e)
                _errorMessage.value = "开始学习失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCurrentWordChoices() {
        viewModelScope.launch {
            try {
                val choices = studyService.getCurrentWordChoices()
                _choices.value = choices
            } catch (e: Exception) {
                AppLogger.e("Error loading word choices", e)
                _choices.value = emptyList()
            }
        }
    }

    fun handleChoice(choice: ChoiceOption) {
        viewModelScope.launch {
            try {
                studyService.handleUserChoice(choice)
                // 清除选择项，因为已经回答了
                _choices.value = emptyList()
            } catch (e: Exception) {
                AppLogger.e("Error handling choice", e)
            }
        }
    }

    /**
     * 处理短语答案（认识/不认识）
     */
    fun handlePhraseAnswer(userKnows: Boolean) {
        viewModelScope.launch {
            try {
                studyService.handlePhraseAnswer(userKnows)
            } catch (e: Exception) {
                AppLogger.e("Error handling phrase answer", e)
            }
        }
    }

    fun moveToNextWord() {
        viewModelScope.launch {
            try {
                studyService.moveToNextWord()
            } catch (e: Exception) {
                AppLogger.e("Error moving to next word", e)
            }
        }
    }

    fun moveToPreviousWord() {
        studyService.moveToPreviousWord()
    }

    fun finishStudy() {
        viewModelScope.launch {
            try {
                studyService.finishStudySession()
            } catch (e: Exception) {
                AppLogger.e("Error finishing study session", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

// ViewModelFactory for StudyViewModel
class StudyViewModelFactory(
    private val studyService: StudyService,
    private val deckRepository: DeckRepository,
    private val wordRepository: WordRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            return StudyViewModel(studyService, deckRepository, wordRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
