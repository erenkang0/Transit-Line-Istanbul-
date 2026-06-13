package com.transitline.istanbul.data.repository

import com.transitline.istanbul.data.local.dao.BusDao
import com.transitline.istanbul.data.local.entity.BusLineEntity
import com.transitline.istanbul.data.local.entity.BusStopEntity
import com.transitline.istanbul.data.remote.LiveDataSource
import com.transitline.istanbul.domain.model.BusArrival
import com.transitline.istanbul.domain.model.BusLine
import com.transitline.istanbul.domain.model.BusSearchResults
import com.transitline.istanbul.domain.model.BusStop
import com.transitline.istanbul.domain.model.DirectionStep
import com.transitline.istanbul.domain.model.LineResult
import com.transitline.istanbul.domain.model.StopResult
import java.time.LocalTime

/**
 * Bus search and detail logic. Arrivals come from [live] when a real-time source
 * is connected; otherwise they are derived from the bundled schedule (and the UI
 * labels them as scheduled, never as an error).
 */
class BusRepository(
    private val busDao: BusDao,
    private val live: LiveDataSource,
    private val nowMinutes: () -> Int = { LocalTime.now().let { it.hour * 60 + it.minute } },
) {

    suspend fun search(query: String): BusSearchResults {
        val q = query.trim()
        if (q.isEmpty()) return BusSearchResults.EMPTY
        val stops = busDao.searchStops(q).map { it.toDomain() }
        val lines = busDao.searchLines(q).map { it.toDomain() }
        return BusSearchResults(lines = lines, stops = stops)
    }

    suspend fun stopResult(stopId: String): StopResult? {
        val stop = busDao.stopById(stopId) ?: return null
        val lineEntities = busDao.linesForStop(stopId)
        val arrivals = arrivalsFor(stop, lineEntities)
        return StopResult(
            stop = stop.toDomain(),
            linesAtStop = lineEntities.map { it.toDomain() },
            arrivals = arrivals,
        )
    }

    private suspend fun arrivalsFor(stop: BusStopEntity, lines: List<BusLineEntity>): List<BusArrival> {
        // Prefer a connected live source; fall back to the schedule silently.
        live.arrivalsAtStop(stop.code)?.let { liveArrivals ->
            val byId = lines.associateBy { it.id }
            return liveArrivals
                .mapNotNull { la -> byId[la.lineId]?.let { BusArrival(it.toDomain(), la.etaMinutes, isLive = true) } }
                .sortedBy { it.etaMinutes }
        }

        val now = nowMinutes()
        val offsets = busDao.routeStopsAtStop(stop.id).associate { it.lineId to it.offsetMin }
        val result = mutableListOf<BusArrival>()
        for (line in lines) {
            val offset = offsets[line.id] ?: continue
            nextEtasAtStop(line, offset, now).forEach { eta ->
                result += BusArrival(line.toDomain(), eta, isLive = false)
            }
        }
        return result.sortedBy { it.etaMinutes }.take(8)
    }

    private fun nextEtasAtStop(line: BusLineEntity, offsetMin: Int, now: Int, count: Int = 3): List<Int> {
        val etas = mutableListOf<Int>()
        var departure = line.firstDepartureMin
        while (departure <= line.lastDepartureMin && etas.size < count) {
            val eta = (departure + offsetMin) - now
            if (eta in 0..120) etas += eta
            departure += line.headwayMin
        }
        return etas
    }

    suspend fun lineResult(lineId: String): LineResult? {
        val line = busDao.lineById(lineId) ?: return null
        val routeStops = busDao.routeStopsForLine(lineId)
        val stopsById = busDao.stopsByIds(routeStops.map { it.stopId }).associateBy { it.id }
        val orderedStops = routeStops.mapNotNull { stopsById[it.stopId]?.toDomain() }
        return LineResult(
            line = line.toDomain(routeStops.map { it.stopId }),
            departures = originDepartures(line).map(::formatMinutes),
            stops = orderedStops,
        )
    }

    /** Step-by-step directions from the line origin to the tapped stop. */
    suspend fun directionsTo(lineId: String, toStopId: String): List<DirectionStep> {
        val line = busDao.lineById(lineId) ?: return emptyList()
        val routeStops = busDao.routeStopsForLine(lineId)
        val targetIndex = routeStops.indexOfFirst { it.stopId == toStopId }
        if (targetIndex <= 0) return emptyList()
        val stopsById = busDao.stopsByIds(routeStops.map { it.stopId }).associateBy { it.id }
        val boardName = stopsById[routeStops.first().stopId]?.name ?: line.originName
        val alightName = stopsById[toStopId]?.name ?: line.destinationName
        return listOf(
            DirectionStep.Board(line.code, boardName),
            DirectionStep.Ride(targetIndex),
            DirectionStep.Alight(alightName),
        )
    }

    private fun originDepartures(line: BusLineEntity): List<Int> {
        val out = mutableListOf<Int>()
        var t = line.firstDepartureMin
        while (t <= line.lastDepartureMin) {
            out += t
            t += line.headwayMin
        }
        return out
    }

    private fun formatMinutes(minuteOfDay: Int): String {
        val m = minuteOfDay % (24 * 60)
        return "%02d:%02d".format(m / 60, m % 60)
    }

    private fun BusStopEntity.toDomain() = BusStop(id, code, name)

    private fun BusLineEntity.toDomain(stopIds: List<String> = emptyList()) =
        BusLine(id, code, originName, destinationName, stopIds)
}
