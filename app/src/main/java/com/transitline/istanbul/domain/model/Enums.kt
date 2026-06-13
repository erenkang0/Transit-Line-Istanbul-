package com.transitline.istanbul.domain.model

/** Top-level content the home screen switches between. No bottom nav bar exists. */
enum class TransitMode { METRO, BUS }

/** App language. SYSTEM follows the device locale; TR/EN force a locale. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    TURKISH("tr"),
    ENGLISH("en"),
}

/** User-facing text scale, applied via [androidx.compose.ui.unit.Density.fontScale]. */
enum class TextSize(val scale: Float) {
    STANDARD(1.0f),
    LARGE(1.18f),
    EXTRA_LARGE(1.36f),
}

/** How two metro nodes are connected for a transfer. Drives the node glyph. */
enum class TransferKind {
    /** Same physical hub served by 2+ lines — drawn as a rounded diamond. */
    INTERCHANGE,

    /** Separate stations linked by a short walk — drawn as a pill with 3 dots. */
    WALK,
}

/** What a favorite points at, so it can be restored from an export file. */
enum class FavoriteType { METRO_STATION, BUS_STOP, BUS_LINE }
