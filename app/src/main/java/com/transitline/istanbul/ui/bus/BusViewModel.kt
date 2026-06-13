package com.transitline.istanbul.ui.bus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitline.istanbul.data.repository.BusRepository
import com.transitline.istanbul.data.repository.FavoritesRepository
import com.transitline.istanbul.domain.model.BusLine
import com.transitline.istanbul.domain.model.BusSearchResults
import com.transitline.istanbul.domain.model.BusStop
import com.transitline.istanbul.domain.model.DirectionStep
import com.transitline.istanbul.domain.model.FavoriteType
import com.transitline.istanbul.domain.model.LineResult
import com.transitline.istanbul.domain.model.StopResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the bus detail area is currently showing. */
sealed interface BusDetail {
    data class Stop(val result: StopResult) : BusDetail
    data class Line(val result: LineResult) : BusDetail
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BusViewModel(
    private val busRepository: BusRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<BusSearchResults> = _query
        .debounce(180L)
        .map { it.trim() }
        .distinctUntilChanged()
        .mapLatest { busRepository.search(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BusSearchResults.EMPTY)

    private val _detail = MutableStateFlow<BusDetail?>(null)
    val detail: StateFlow<BusDetail?> = _detail.asStateFlow()

    private val _directions = MutableStateFlow<List<DirectionStep>>(emptyList())
    val directions: StateFlow<List<DirectionStep>> = _directions.asStateFlow()

    val favoriteKeys: StateFlow<Set<String>> = favoritesRepository.favoriteKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun setQuery(value: String) { _query.value = value }
    fun clearQuery() { _query.value = "" }

    fun openStop(stopId: String) {
        viewModelScope.launch {
            busRepository.stopResult(stopId)?.let { _detail.value = BusDetail.Stop(it) }
        }
    }

    fun openLine(lineId: String) {
        viewModelScope.launch {
            busRepository.lineResult(lineId)?.let { _detail.value = BusDetail.Line(it) }
        }
    }

    fun closeDetail() {
        _detail.value = null
        _directions.value = emptyList()
    }

    fun requestDirections(lineId: String, toStopId: String) {
        viewModelScope.launch { _directions.value = busRepository.directionsTo(lineId, toStopId) }
    }

    fun clearDirections() { _directions.value = emptyList() }

    fun toggleStopFavorite(stop: BusStop) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteType.BUS_STOP, stop.id, stop.name) }
    }

    fun toggleLineFavorite(line: BusLine) {
        viewModelScope.launch { favoritesRepository.toggle(FavoriteType.BUS_LINE, line.id, line.code) }
    }
}
