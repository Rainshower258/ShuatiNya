package com.example.english.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.DeckReviewInfo
import com.example.english.data.repository.ReviewRepository
import com.example.english.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 复习中心 ViewModel - 按词库分组显示
 */
class ReviewCenterViewModel(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _deckReviewList = MutableStateFlow<List<DeckReviewInfo>>(emptyList())
    val deckReviewList: StateFlow<List<DeckReviewInfo>> = _deckReviewList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        AppLogger.d("ReviewCenterViewModel initialized")
        loadDeckReviewInfo()
    }

    /**
     * 加载所有词库的复习信息
     */
    fun loadDeckReviewInfo() {
        AppLogger.d("loadDeckReviewInfo() called")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            AppLogger.d("Starting to load deck review info...")
            try {
                val deckInfoList = reviewRepository.getAllDeckReviewInfo()
                AppLogger.d("Loaded ${deckInfoList.size} decks successfully")
                _deckReviewList.value = deckInfoList
            } catch (e: Exception) {
                AppLogger.e("Error loading deck review info", e)
                _errorMessage.value = "加载失败：${e.message}"
            } finally {
                _isLoading.value = false
                AppLogger.d("Loading completed, isLoading = false")
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        AppLogger.d("refresh() called")
        loadDeckReviewInfo()
    }
}

/**
 * ViewModelFactory
 */
class ReviewCenterViewModelFactory(
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewCenterViewModel::class.java)) {
            return ReviewCenterViewModel(reviewRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

