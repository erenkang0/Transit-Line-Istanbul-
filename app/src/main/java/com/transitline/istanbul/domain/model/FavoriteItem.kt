package com.transitline.istanbul.domain.model

import androidx.compose.runtime.Immutable

/** A saved favorite, resolved enough to display and to navigate back to. */
@Immutable
data class FavoriteItem(
    val type: FavoriteType,
    val refId: String,
    val label: String,
)
