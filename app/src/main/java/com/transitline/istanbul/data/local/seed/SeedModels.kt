package com.transitline.istanbul.data.local.seed

import kotlinx.serialization.Serializable

/**
 * Shapes of the bundled `assets/seed/*.json` files. Keeping the seed as data
 * (not code) makes the offline network easy to extend or replace with a full
 * İBB GTFS import later, without touching the app.
 */
@Serializable
data class MetroSeed(
    val lines: List<LineSeed>,
    val stations: List<StationSeed>,
    val walkTransfers: List<WalkSeed> = emptyList(),
)

@Serializable
data class LineSeed(
    val id: String,
    val code: String,
    val nameTr: String,
    val nameEn: String,
    val colorHex: String,
    val stations: List<String>,
)

@Serializable
data class StationSeed(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val lat: Double? = null,
    val lon: Double? = null,
)

@Serializable
data class WalkSeed(
    val from: String,
    val to: String,
)

@Serializable
data class BusSeed(
    val stops: List<StopSeed>,
    val lines: List<BusLineSeed>,
)

@Serializable
data class StopSeed(
    val id: String,
    val code: String,
    val name: String,
)

@Serializable
data class BusLineSeed(
    val id: String,
    val code: String,
    val originName: String,
    val destinationName: String,
    val firstDepartureMin: Int,
    val lastDepartureMin: Int,
    val headwayMin: Int,
    val stops: List<RouteStopSeed>,
)

@Serializable
data class RouteStopSeed(
    val stopId: String,
    val offsetMin: Int,
)
