package com.transitline.istanbul.domain.model

/**
 * All user preferences. Persisted in DataStore and surfaced as a single
 * immutable snapshot so the theme and screens can react to it as one unit.
 */
data class AppSettings(
    val onboarded: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val textSize: TextSize = TextSize.STANDARD,
    val easyMode: Boolean = false,
    val highContrast: Boolean = false,
    /** Material You. Metro line colors are never affected by this. */
    val dynamicColor: Boolean = true,
    /** Forces OLED-black UI and disables GPS the instant it is enabled. */
    val powerSaving: Boolean = false,
    val gpsIntervalMinutes: Int = 10,
)
