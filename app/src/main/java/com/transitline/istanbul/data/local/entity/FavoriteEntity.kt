package com.transitline.istanbul.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite",
    indices = [Index(value = ["type", "refId"], unique = true)],
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** [com.transitline.istanbul.domain.model.FavoriteType] name. */
    val type: String,
    val refId: String,
    val label: String,
    val createdAt: Long,
)
