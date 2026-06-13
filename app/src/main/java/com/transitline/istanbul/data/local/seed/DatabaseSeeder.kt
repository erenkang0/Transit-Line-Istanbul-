package com.transitline.istanbul.data.local.seed

import android.content.Context
import com.transitline.istanbul.data.local.TransitDatabase
import com.transitline.istanbul.data.local.entity.BusLineEntity
import com.transitline.istanbul.data.local.entity.BusRouteStopEntity
import com.transitline.istanbul.data.local.entity.BusStopEntity
import com.transitline.istanbul.data.local.entity.MetroLineEntity
import com.transitline.istanbul.data.local.entity.MetroLineStationCrossRef
import com.transitline.istanbul.data.local.entity.MetroStationEntity
import com.transitline.istanbul.data.local.entity.MetroWalkTransferEntity
import kotlinx.serialization.json.Json

/**
 * Loads the bundled offline network into Room on first launch. Idempotent: each
 * domain is only seeded when its table is empty, so it is safe to call on every
 * start. This is what makes the app respond instantly with no network.
 */
class DatabaseSeeder(
    private val context: Context,
    private val db: TransitDatabase,
    private val json: Json,
) {
    suspend fun seedIfNeeded() {
        if (db.metroDao().lineCount() == 0) seedMetro()
        if (db.busDao().lineCount() == 0) seedBus()
    }

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader().use { it.readText() }

    private suspend fun seedMetro() {
        val seed = json.decodeFromString(MetroSeed.serializer(), readAsset("seed/metro.json"))
        val dao = db.metroDao()
        dao.insertStations(seed.stations.map { MetroStationEntity(it.id, it.name, it.x, it.y, it.lat, it.lon) })
        dao.insertLines(
            seed.lines.mapIndexed { index, l ->
                MetroLineEntity(l.id, l.code, l.nameTr, l.nameEn, l.colorHex, index)
            },
        )
        dao.insertLineStations(
            seed.lines.flatMap { line ->
                line.stations.mapIndexed { seq, sid -> MetroLineStationCrossRef(line.id, sid, seq) }
            },
        )
        dao.insertWalkTransfers(
            seed.walkTransfers.map { MetroWalkTransferEntity(fromStationId = it.from, toStationId = it.to) },
        )
    }

    private suspend fun seedBus() {
        val seed = json.decodeFromString(BusSeed.serializer(), readAsset("seed/bus.json"))
        val dao = db.busDao()
        dao.insertStops(seed.stops.map { BusStopEntity(it.id, it.code, it.name) })
        dao.insertLines(
            seed.lines.map {
                BusLineEntity(
                    id = it.id,
                    code = it.code,
                    originName = it.originName,
                    destinationName = it.destinationName,
                    firstDepartureMin = it.firstDepartureMin,
                    lastDepartureMin = it.lastDepartureMin,
                    headwayMin = it.headwayMin,
                )
            },
        )
        dao.insertRouteStops(
            seed.lines.flatMap { line ->
                line.stops.mapIndexed { seq, rs -> BusRouteStopEntity(line.id, rs.stopId, seq, rs.offsetMin) }
            },
        )
    }
}
