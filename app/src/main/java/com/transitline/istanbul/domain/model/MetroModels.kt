package com.transitline.istanbul.domain.model

import androidx.compose.runtime.Immutable

/**
 * A station node in the schematic graph. Coordinates are normalized (0..1) so
 * the Canvas can map them into any viewport without the data knowing about px.
 */
@Immutable
data class MetroStation(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val lat: Double? = null,
    val lon: Double? = null,
)

/**
 * A metro/rail line. [colorArgb] is the official line color and is intentionally
 * stored with the data (never derived from the Material You scheme).
 * [stationIds] is the ordered list of stops, drawn as a single rounded polyline.
 */
@Immutable
data class MetroLine(
    val id: String,
    val code: String,
    val nameTr: String,
    val nameEn: String,
    val colorArgb: Long,
    val stationIds: List<String>,
)

/** A short walking connection between two distinct station nodes. */
@Immutable
data class WalkTransfer(
    val fromStationId: String,
    val toStationId: String,
)

/**
 * The whole schematic network. Precomputed lookup maps keep the per-frame Canvas
 * hit-testing and styling O(1) instead of scanning lists while drawing.
 */
@Immutable
data class MetroNetwork(
    val lines: List<MetroLine>,
    val stations: List<MetroStation>,
    val walkTransfers: List<WalkTransfer>,
) {
    val stationsById: Map<String, MetroStation> = stations.associateBy { it.id }

    val linesByStationId: Map<String, List<MetroLine>> = buildMap {
        lines.forEach { line ->
            line.stationIds.forEach { sid ->
                put(sid, (get(sid) ?: emptyList()) + line)
            }
        }
    }

    /** A node served by 2+ lines is a major interchange (rounded-diamond glyph). */
    fun isInterchange(stationId: String): Boolean =
        (linesByStationId[stationId]?.size ?: 0) >= 2

    fun linesFor(stationId: String): List<MetroLine> =
        linesByStationId[stationId].orEmpty()

    companion object {
        val EMPTY = MetroNetwork(emptyList(), emptyList(), emptyList())
    }
}
