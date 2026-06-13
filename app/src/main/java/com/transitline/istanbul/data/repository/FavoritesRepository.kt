package com.transitline.istanbul.data.repository

import com.transitline.istanbul.data.local.dao.FavoriteDao
import com.transitline.istanbul.data.local.entity.FavoriteEntity
import com.transitline.istanbul.domain.model.FavoriteItem
import com.transitline.istanbul.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepository(private val dao: FavoriteDao) {

    /** Stable "TYPE:refId" keys, so screens can cheaply check membership. */
    val favoriteKeys: Flow<Set<String>> = dao.observeAll().map { rows ->
        rows.mapTo(HashSet()) { key(it.type, it.refId) }
    }

    /** Full favorites list for the favorites sheet, newest first. */
    val favorites: Flow<List<FavoriteItem>> = dao.observeAll().map { rows ->
        rows.mapNotNull { row ->
            val type = runCatching { FavoriteType.valueOf(row.type) }.getOrNull() ?: return@mapNotNull null
            FavoriteItem(type, row.refId, row.label)
        }
    }

    suspend fun toggle(type: FavoriteType, refId: String, label: String) {
        if (dao.exists(type.name, refId)) {
            dao.delete(type.name, refId)
        } else {
            dao.upsert(
                FavoriteEntity(
                    type = type.name,
                    refId = refId,
                    label = label,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    companion object {
        fun key(type: FavoriteType, refId: String): String = key(type.name, refId)
        private fun key(type: String, refId: String): String = "$type:$refId"
    }
}
