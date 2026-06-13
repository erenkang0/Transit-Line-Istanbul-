package com.transitline.istanbul.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.transitline.istanbul.domain.model.AppLanguage
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.domain.model.TextSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Single source of truth for user preferences, backed by Preferences DataStore. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val LANGUAGE = stringPreferencesKey("language")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val EASY = booleanPreferencesKey("easy_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val POWER = booleanPreferencesKey("power_saving")
        val GPS = intPreferencesKey("gps_interval")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toSettings() }

    private fun Preferences.toSettings() = AppSettings(
        onboarded = this[Keys.ONBOARDED] ?: false,
        language = this[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
            ?: AppLanguage.SYSTEM,
        textSize = this[Keys.TEXT_SIZE]?.let { runCatching { TextSize.valueOf(it) }.getOrNull() }
            ?: TextSize.STANDARD,
        easyMode = this[Keys.EASY] ?: false,
        highContrast = this[Keys.HIGH_CONTRAST] ?: false,
        dynamicColor = this[Keys.DYNAMIC] ?: true,
        powerSaving = this[Keys.POWER] ?: false,
        gpsIntervalMinutes = this[Keys.GPS] ?: 10,
    )

    suspend fun current(): AppSettings = settings.first()

    /** One-shot synchronous read used only by attachBaseContext for the locale. */
    fun currentBlocking(): AppSettings = runBlocking { settings.first() }

    suspend fun setOnboarded(value: Boolean) = update { it[Keys.ONBOARDED] = value }
    suspend fun setLanguage(value: AppLanguage) = update { it[Keys.LANGUAGE] = value.name }
    suspend fun setTextSize(value: TextSize) = update { it[Keys.TEXT_SIZE] = value.name }
    suspend fun setEasyMode(value: Boolean) = update { it[Keys.EASY] = value }
    suspend fun setHighContrast(value: Boolean) = update { it[Keys.HIGH_CONTRAST] = value }
    suspend fun setDynamicColor(value: Boolean) = update { it[Keys.DYNAMIC] = value }
    suspend fun setPowerSaving(value: Boolean) = update { it[Keys.POWER] = value }
    suspend fun setGpsInterval(minutes: Int) = update { it[Keys.GPS] = minutes }

    /** Used by import to apply a restored preference set in one transaction. */
    suspend fun replaceAll(s: AppSettings) = update {
        it[Keys.ONBOARDED] = s.onboarded
        it[Keys.LANGUAGE] = s.language.name
        it[Keys.TEXT_SIZE] = s.textSize.name
        it[Keys.EASY] = s.easyMode
        it[Keys.HIGH_CONTRAST] = s.highContrast
        it[Keys.DYNAMIC] = s.dynamicColor
        it[Keys.POWER] = s.powerSaving
        it[Keys.GPS] = s.gpsIntervalMinutes
    }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
