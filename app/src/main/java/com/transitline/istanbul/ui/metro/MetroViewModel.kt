package com.transitline.istanbul.ui.metro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitline.istanbul.core.location.LocationProvider
import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.data.repository.FavoritesRepository
import com.transitline.istanbul.data.repository.MetroRepository
import com.transitline.istanbul.domain.model.FavoriteType
import com.transitline.istanbul.domain.model.MetroNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetroViewModel(
    private val metroRepository: MetroRepository,
    private val settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    val network: StateFlow<MetroNetwork> = metroRepository.networkFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MetroNetwork.EMPTY)

    val favoriteKeys: StateFlow<Set<String>> = favoritesRepository.favoriteKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    var selectedStationId by mutableStateOf<String?>(null)
        private set
    var originId by mutableStateOf<String?>(null)
        private set
    var destinationId by mutableStateOf<String?>(null)
        private set
    var nearestStationId by mutableStateOf<String?>(null)
        private set

    init {
        // Re-locate on the configured cadence. collectLatest restarts the loop
        // whenever Power Saving or the interval changes; Power Saving stops GPS.
        viewModelScope.launch {
            settingsRepository.settings
                .map { it.powerSaving to it.gpsIntervalMinutes }
                .distinctUntilChanged()
                .collectLatest { (powerSaving, intervalMin) ->
                    if (powerSaving) {
                        nearestStationId = null
                        return@collectLatest
                    }
                    while (true) {
                        if (locationProvider.hasPermission()) refreshNearest(forceRequest = false)
                        delay(intervalMin.coerceAtLeast(1) * 60_000L)
                    }
                }
        }
    }

    fun select(stationId: String) { selectedStationId = stationId }
    fun clearSelection() { selectedStationId = null }

    fun useSelectionAsOrigin() {
        selectedStationId?.let { originId = it }
        selectedStationId = null
    }

    fun useSelectionAsDestination() {
        selectedStationId?.let { destinationId = it }
        selectedStationId = null
    }

    fun clearRoute() {
        originId = null
        destinationId = null
    }

    /** Triggered by the "find nearest" button after permission is granted. */
    fun locateNow() {
        viewModelScope.launch { refreshNearest(forceRequest = true) }
    }

    private suspend fun refreshNearest(forceRequest: Boolean) {
        val location = if (forceRequest) {
            locationProvider.requestSingle() ?: locationProvider.lastKnown()
        } else {
            locationProvider.lastKnown()
        } ?: return
        nearestStationId = metroRepository.nearestStation(location.latitude, location.longitude)?.id
    }

    fun toggleFavorite(stationId: String, label: String) {
        viewModelScope.launch {
            favoritesRepository.toggle(FavoriteType.METRO_STATION, stationId, label)
        }
    }
}
