/*
 * MIT License
 * Copyright (c) 2025 sun6 (Rainshower258)
 * See LICENSE file in the project root for full license information.
 */
package com.example.english.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.english.data.repository.DeckRepository
import com.example.english.ui.screens.deck_list.DeckListViewModel

/**
 * 一个临时的ViewModel工厂，用于手动创建ViewModel实例.
 *
 * @param deckRepository 词库仓库的实现
 */
class ViewModelFactory(private val deckRepository: DeckRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeckListViewModel(deckRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

