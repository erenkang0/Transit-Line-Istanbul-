package com.transitline.istanbul.data.repository

import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.data.local.dao.FavoriteDao
import com.transitline.istanbul.data.local.entity.FavoriteEntity
import com.transitline.istanbul.domain.model.AppLanguage
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.domain.model.TextSize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Account-free portability: serializes preferences + favorites to a small JSON
 * document the user can save anywhere and restore on another device. There is no
 * login; this file IS the user's portable state.
 */
class BackupManager(
    private val settings: SettingsRepository,
    private val favoriteDao: FavoriteDao,
    private val json: Json,
) {

    @Serializable
    data class Backup(
        val version: Int = 1,
        val settings: SettingsBackup,
        val favorites: List<FavoriteBackup>,
    )

    @Serializable
    data class SettingsBackup(
        val language: String,
        val textSize: String,
        val easyMode: Boolean,
        val highContrast: Boolean,
        val dynamicColor: Boolean,
        val powerSaving: Boolean,
        val gpsIntervalMinutes: Int,
    )

    @Serializable
    data class FavoriteBackup(
        val type: String,
        val refId: String,
        val label: String,
        val createdAt: Long,
    )

    suspend fun export(): String {
        val s = settings.current()
        val favorites = favoriteDao.getAll().map { FavoriteBackup(it.type, it.refId, it.label, it.createdAt) }
        val backup = Backup(
            settings = SettingsBackup(
                language = s.language.name,
                textSize = s.textSize.name,
                easyMode = s.easyMode,
                highContrast = s.highContrast,
                dynamicColor = s.dynamicColor,
                powerSaving = s.powerSaving,
                gpsIntervalMinutes = s.gpsIntervalMinutes,
            ),
            favorites = favorites,
        )
        return json.encodeToString(Backup.serializer(), backup)
    }

    /** Returns true on success. Malformed input fails gracefully (no crash). */
    suspend fun import(text: String): Boolean {
        val backup = runCatching { json.decodeFromString(Backup.serializer(), text) }.getOrNull()
            ?: return false
        val b = backup.settings
        settings.replaceAll(
            AppSettings(
                onboarded = true,
                language = runCatching { AppLanguage.valueOf(b.language) }.getOrDefault(AppLanguage.SYSTEM),
                textSize = runCatching { TextSize.valueOf(b.textSize) }.getOrDefault(TextSize.STANDARD),
                easyMode = b.easyMode,
                highContrast = b.highContrast,
                dynamicColor = b.dynamicColor,
                powerSaving = b.powerSaving,
                gpsIntervalMinutes = b.gpsIntervalMinutes,
            ),
        )
        backup.favorites.forEach {
            favoriteDao.upsert(FavoriteEntity(type = it.type, refId = it.refId, label = it.label, createdAt = it.createdAt))
        }
        return true
    }
}
