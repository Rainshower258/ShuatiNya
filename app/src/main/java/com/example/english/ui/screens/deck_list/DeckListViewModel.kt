package com.example.english.ui.screens.deck_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.english.data.model.Deck
import com.example.english.data.repository.DeckRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckListViewModel(private val deckRepository: DeckRepository) : ViewModel() {

    val decks: StateFlow<List<Deck>> = deckRepository.getAllActiveDecks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDeck(name: String, deckType: com.example.english.data.model.DeckType = com.example.english.data.model.DeckType.VOCABULARY) {
        viewModelScope.launch {
            deckRepository.createDeck(name, deckType = deckType)
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            deckRepository.permanentlyDeleteDeck(deck)
        }
    }
}

