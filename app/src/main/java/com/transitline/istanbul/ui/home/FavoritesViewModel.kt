package com.transitline.istanbul.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitline.istanbul.data.repository.FavoritesRepository
import com.transitline.istanbul.domain.model.FavoriteItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {

    val favorites: StateFlow<List<FavoriteItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(item: FavoriteItem) {
        viewModelScope.launch { repository.toggle(item.type, item.refId, item.label) }
    }
}
