package com.example.english.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.DeckReviewInfo
import com.example.english.data.model.DeckStudyRecord
import com.example.english.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 词库详情 ViewModel - 显示单个词库的学习统计和记录
 */
class DeckDetailViewModel(
    private val deckId: Long,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _deckReviewInfo = MutableStateFlow<DeckReviewInfo?>(null)
    val deckReviewInfo: StateFlow<DeckReviewInfo?> = _deckReviewInfo.asStateFlow()

    private val _studyRecords = MutableStateFlow<List<DeckStudyRecord>>(emptyList())
    val studyRecords: StateFlow<List<DeckStudyRecord>> = _studyRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow<DeckStudyRecord?>(null)
    val showDeleteDialog: StateFlow<DeckStudyRecord?> = _showDeleteDialog.asStateFlow()

    init {
        loadData()
    }

    /**
     * 加载词库详情数据
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 加载词库复习信息
                val deckInfo = reviewRepository.getDeckReviewInfo(deckId)
                _deckReviewInfo.value = deckInfo

                // 加载学习记录
                val records = reviewRepository.getDeckStudyRecords(deckId)
                _studyRecords.value = records
            } catch (e: Exception) {
                _errorMessage.value = "加载失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 显示删除确认对话框
     */
    fun showDeleteConfirmation(record: DeckStudyRecord) {
        _showDeleteDialog.value = record
    }

    /**
     * 隐藏删除确认对话框
     */
    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    /**
     * 删除学习记录
     */
    fun deleteRecord(record: DeckStudyRecord) {
        viewModelScope.launch {
            try {
                reviewRepository.deleteStudyRecord(record.sessionId)
                // 重新加载数据
                loadData()
                dismissDeleteDialog()
            } catch (e: Exception) {
                _errorMessage.value = "删除失败：${e.message}"
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadData()
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * ViewModelFactory
 */
class DeckDetailViewModelFactory(
    private val deckId: Long,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckDetailViewModel::class.java)) {
            return DeckDetailViewModel(deckId, reviewRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

