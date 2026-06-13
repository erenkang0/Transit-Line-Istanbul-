package com.transitline.istanbul.data.repository

import com.transitline.istanbul.data.local.dao.MetroDao
import com.transitline.istanbul.domain.model.MetroLine
import com.transitline.istanbul.domain.model.MetroNetwork
import com.transitline.istanbul.domain.model.MetroStation
import com.transitline.istanbul.domain.model.WalkTransfer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Assembles the schematic [MetroNetwork] from Room and caches it in memory.
 * The network is small and static, so a single in-memory snapshot keeps the
 * Canvas fed without touching disk on every frame or recomposition.
 */
class MetroRepository(private val metroDao: MetroDao) {

    private val mutex = Mutex()
    @Volatile private var cached: MetroNetwork? = null

    suspend fun network(): MetroNetwork {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: build().also { cached = it }
        }
    }

    private suspend fun build(): MetroNetwork {
        val lines = metroDao.lines()
        val stations = metroDao.stations()
        val crossRefs = metroDao.lineStations().groupBy { it.lineId }
        val walks = metroDao.walkTransfers()

        val domainLines = lines.map { line ->
            MetroLine(
                id = line.id,
                code = line.code,
                nameTr = line.nameTr,
                nameEn = line.nameEn,
                colorArgb = parseArgb(line.colorHex),
                stationIds = crossRefs[line.id].orEmpty().sortedBy { it.seq }.map { it.stationId },
            )
        }
        val domainStations = stations.map {
            MetroStation(it.id, it.name, it.x, it.y, it.lat, it.lon)
        }
        val domainWalks = walks.map { WalkTransfer(it.fromStationId, it.toStationId) }
        return MetroNetwork(domainLines, domainStations, domainWalks)
    }

    /** Nearest station to a coordinate, for the GPS highlight feature. */
    suspend fun nearestStation(lat: Double, lon: Double): MetroStation? {
        val net = network()
        return net.stations
            .filter { it.lat != null && it.lon != null }
            .minByOrNull { haversineMeters(lat, lon, it.lat!!, it.lon!!) }
    }

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
