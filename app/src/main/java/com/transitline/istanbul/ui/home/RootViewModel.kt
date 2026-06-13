package com.transitline.istanbul.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitline.istanbul.core.connectivity.ConnectivityObserver
import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Owns app-wide state: the settings snapshot (drives the theme) and online state. */
class RootViewModel(
    settingsRepository: SettingsRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    /** null until the first read completes; the splash stays up until then. */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isOnline: StateFlow<Boolean> = connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
}
