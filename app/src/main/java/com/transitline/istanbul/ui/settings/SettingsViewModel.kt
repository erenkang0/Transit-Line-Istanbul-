package com.transitline.istanbul.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.data.repository.BackupManager
import com.transitline.istanbul.domain.model.AppLanguage
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.domain.model.TextSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    /** String resource id of a one-shot result message, or null. */
    private val _message = MutableStateFlow<Int?>(null)
    val message: StateFlow<Int?> = _message.asStateFlow()

    fun setPowerSaving(value: Boolean) = launchUpdate { settingsRepository.setPowerSaving(value) }
    fun setDynamicColor(value: Boolean) = launchUpdate { settingsRepository.setDynamicColor(value) }
    fun setHighContrast(value: Boolean) = launchUpdate { settingsRepository.setHighContrast(value) }
    fun setEasyMode(value: Boolean) = launchUpdate { settingsRepository.setEasyMode(value) }
    fun setLanguage(value: AppLanguage) = launchUpdate { settingsRepository.setLanguage(value) }
    fun setTextSize(value: TextSize) = launchUpdate { settingsRepository.setTextSize(value) }
    fun setGpsInterval(minutes: Int) = launchUpdate { settingsRepository.setGpsInterval(minutes) }

    suspend fun buildExport(): String = backupManager.export()

    fun onExported(successMessageRes: Int) { _message.value = successMessageRes }

    fun import(text: String, successRes: Int, errorRes: Int) {
        viewModelScope.launch {
            _message.value = if (backupManager.import(text)) successRes else errorRes
        }
    }

    fun consumeMessage() { _message.value = null }

    private inline fun launchUpdate(crossinline block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
