package com.transitline.istanbul.data.repository

import com.transitline.istanbul.data.local.dao.MetroDao
import com.transitline.istanbul.data.local.entity.MetroLineEntity
import com.transitline.istanbul.data.local.entity.MetroLineStationCrossRef
import com.transitline.istanbul.data.local.entity.MetroStationEntity
import com.transitline.istanbul.data.local.entity.MetroWalkTransferEntity
import com.transitline.istanbul.domain.model.MetroLine
import com.transitline.istanbul.domain.model.MetroNetwork
import com.transitline.istanbul.domain.model.MetroStation
import com.transitline.istanbul.domain.model.WalkTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Assembles the schematic [MetroNetwork] from Room as a reactive Flow. Because it
 * is Flow-based, the map populates the instant the first-launch seed lands —
 * there is no race between the seeder and the screen subscribing.
 */
class MetroRepository(private val metroDao: MetroDao) {

    fun networkFlow(): Flow<MetroNetwork> = combine(
        metroDao.linesFlow(),
        metroDao.stationsFlow(),
        metroDao.lineStationsFlow(),
        metroDao.walkTransfersFlow(),
    ) { lines, stations, crossRefs, walks ->
        buildNetwork(lines, stations, crossRefs, walks)
    }

    /** Nearest station to a coordinate, for the GPS highlight feature. */
    suspend fun nearestStation(lat: Double, lon: Double): MetroStation? {
        return metroDao.stations()
            .map { it.toDomain() }
            .filter { it.lat != null && it.lon != null }
            .minByOrNull { haversineMeters(lat, lon, it.lat!!, it.lon!!) }
    }

    private fun buildNetwork(
        lines: List<MetroLineEntity>,
        stations: List<MetroStationEntity>,
        crossRefs: List<MetroLineStationCrossRef>,
        walks: List<MetroWalkTransferEntity>,
    ): MetroNetwork {
        val byLine = crossRefs.groupBy { it.lineId }
        val domainLines = lines.map { line ->
            MetroLine(
                id = line.id,
                code = line.code,
                nameTr = line.nameTr,
                nameEn = line.nameEn,
                colorArgb = parseArgb(line.colorHex),
                stationIds = byLine[line.id].orEmpty().sortedBy { it.seq }.map { it.stationId },
            )
        }
        return MetroNetwork(
            lines = domainLines,
            stations = stations.map { it.toDomain() },
            walkTransfers = walks.map { WalkTransfer(it.fromStationId, it.toStationId) },
        )
    }

    private fun MetroStationEntity.toDomain() = MetroStation(id, name, x, y, lat, lon)

    private fun parseArgb(hex: String): Long = hex.removePrefix("#").toLong(16)

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
