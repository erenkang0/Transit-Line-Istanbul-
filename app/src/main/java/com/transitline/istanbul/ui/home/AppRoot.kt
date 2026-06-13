package com.transitline.istanbul.ui.home

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.ui.onboarding.OnboardingScreen

/**
 * Top-level content. Routes between one-time onboarding and the home screen based
 * on persisted settings, and keeps the (transparent) system bar icons legible for
 * the active theme — including Power Saving's forced-dark OLED mode.
 */
@Composable
fun AppRoot(settings: AppSettings, isOnline: Boolean) {
    val dark = settings.powerSaving || isSystemInDarkTheme()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    if (!settings.onboarded) {
        OnboardingScreen(onComplete = { /* settings flow flips onboarded → Home shows */ })
    } else {
        HomeScreen(settings = settings, isOnline = isOnline)
    }
}
