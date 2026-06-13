package com.transitline.istanbul.ui.util

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.transitline.istanbul.TransitLineApplication
import com.transitline.istanbul.ui.bus.BusViewModel
import com.transitline.istanbul.ui.home.FavoritesViewModel
import com.transitline.istanbul.ui.home.RootViewModel
import com.transitline.istanbul.ui.metro.MetroViewModel
import com.transitline.istanbul.ui.onboarding.OnboardingViewModel
import com.transitline.istanbul.ui.settings.SettingsViewModel

/** Wires ViewModels to the Application's manual [com.transitline.istanbul.di.AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { RootViewModel(app().container.settingsRepository, app().container.connectivityObserver) }
        initializer { OnboardingViewModel(app().container.settingsRepository) }
        initializer {
            SettingsViewModel(app().container.settingsRepository, app().container.backupManager)
        }
        initializer {
            MetroViewModel(
                app().container.metroRepository,
                app().container.settingsRepository,
                app().container.favoritesRepository,
                app().container.locationProvider,
            )
        }
        initializer {
            BusViewModel(app().container.busRepository, app().container.favoritesRepository)
        }
        initializer { FavoritesViewModel(app().container.favoritesRepository) }
    }
}

private fun CreationExtras.app(): TransitLineApplication =
    this[APPLICATION_KEY] as TransitLineApplication
