package com.transitline.istanbul.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class BusStop(
    val id: String,
    val code: String,
    val name: String,
)

@Immutable
data class BusLine(
    val id: String,
    val code: String,
    val originName: String,
    val destinationName: String,
    /** Ordered stop ids that make up the route. */
    val stopIds: List<String>,
)

/**
 * One bus approaching a stop. [etaMinutes] is minutes from now. [isLive] is false
 * when the value is derived from the offline schedule rather than a live feed —
 * the UI labels it accordingly. Live data is intentionally not wired up yet.
 */
@Immutable
data class BusArrival(
    val line: BusLine,
    val etaMinutes: Int,
    val isLive: Boolean,
)

/** Result of searching a stop: the lines through it and the next arrivals. */
@Immutable
data class StopResult(
    val stop: BusStop,
    val linesAtStop: List<BusLine>,
    val arrivals: List<BusArrival>,
)

/** Result of searching a line: its schedule and ordered route. */
@Immutable
data class LineResult(
    val line: BusLine,
    val departures: List<String>,
    val stops: List<BusStop>,
)

/** Combined autocomplete results for the bus search field. */
@Immutable
data class BusSearchResults(
    val lines: List<BusLine>,
    val stops: List<BusStop>,
) {
    val isEmpty: Boolean get() = lines.isEmpty() && stops.isEmpty()

    companion object {
        val EMPTY = BusSearchResults(emptyList(), emptyList())
    }
}

/** A single step in the vertical, text-based directions panel. */
sealed interface DirectionStep {
    data class Board(val lineCode: String, val atStopName: String) : DirectionStep
    data class Ride(val stopCount: Int) : DirectionStep
    data class Alight(val atStopName: String) : DirectionStep
}
