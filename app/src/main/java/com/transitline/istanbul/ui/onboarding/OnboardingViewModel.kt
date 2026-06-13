package com.transitline.istanbul.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.domain.model.AppLanguage
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.domain.model.TextSize
import kotlinx.coroutines.launch

/** One-time setup: language + comfort/eye-care preferences. */
class OnboardingViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    var language by mutableStateOf(AppLanguage.TURKISH)
        private set
    var textSize by mutableStateOf(TextSize.STANDARD)
        private set
    var easyMode by mutableStateOf(false)
        private set
    var highContrast by mutableStateOf(false)
        private set

    fun selectLanguage(value: AppLanguage) { language = value }

    fun selectTextSize(value: TextSize) { textSize = value }

    fun setEasyMode(value: Boolean) {
        easyMode = value
        // Easy mode nudges text up a step if the user is still on Standard.
        if (value && textSize == TextSize.STANDARD) textSize = TextSize.LARGE
    }

    fun setHighContrast(value: Boolean) { highContrast = value }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.replaceAll(
                AppSettings(
                    onboarded = true,
                    language = language,
                    textSize = textSize,
                    easyMode = easyMode,
                    highContrast = highContrast,
                ),
            )
            onDone()
        }
    }
}
