package com.transitline.istanbul

import com.transitline.istanbul.data.local.dao.BusDao
import com.transitline.istanbul.data.local.entity.BusLineEntity
import com.transitline.istanbul.data.local.entity.BusRouteStopEntity
import com.transitline.istanbul.data.local.entity.BusStopEntity
import com.transitline.istanbul.data.remote.OfflineOnlyLiveDataSource
import com.transitline.istanbul.data.repository.BusRepository
import com.transitline.istanbul.domain.model.DirectionStep
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusRepositoryTest {

    // 05:00 = 300 minutes-of-day.
    private val stops = listOf(
        BusStopEntity("s1", "12345", "Kadıköy"),
        BusStopEntity("s2", "12347", "Acıbadem"),
    )
    private val lines = listOf(
        BusLineEntity("L1", "34", "A", "B", firstDepartureMin = 300, lastDepartureMin = 360, headwayMin = 10),
        BusLineEntity("L2", "15", "C", "D", firstDepartureMin = 300, lastDepartureMin = 360, headwayMin = 12),
    )
    private val routeStops = listOf(
        BusRouteStopEntity("L1", "s1", 0, 0),
        BusRouteStopEntity("L1", "s2", 1, 5),
        BusRouteStopEntity("L2", "s2", 0, 0),
        BusRouteStopEntity("L2", "s1", 1, 8),
    )

    private fun repo(now: Int) = BusRepository(FakeBusDao(stops, lines, routeStops), OfflineOnlyLiveDataSource()) { now }

    @Test
    fun `scheduled arrivals are computed relative to now and labelled offline`() = runTest {
        // now = 05:00. L1 reaches s2 at 05:05 (eta 5); L2 departs s2 at 05:00 (eta 0).
        val result = repo(now = 300).stopResult("s2")!!
        assertEquals("Acıbadem", result.stop.name)
        // Lines through the stop, sorted by code.
        assertEquals(listOf("15", "34"), result.linesAtStop.map { it.code })
        // Soonest arrival is L2 at eta 0, and nothing is live yet.
        assertEquals(0, result.arrivals.first().etaMinutes)
        assertTrue(result.arrivals.all { !it.isLive })
        // ETAs are sorted ascending.
        assertEquals(result.arrivals.map { it.etaMinutes }.sorted(), result.arrivals.map { it.etaMinutes })
    }

    @Test
    fun `past departures are not shown as arrivals`() = runTest {
        // now = 06:30, after the last trip (06:00) — no upcoming arrivals.
        val result = repo(now = 390).stopResult("s2")!!
        assertTrue(result.arrivals.isEmpty())
    }

    @Test
    fun `line result lists origin departures and ordered route`() = runTest {
        val result = repo(now = 300).lineResult("L1")!!
        assertEquals("05:00", result.departures.first())
        assertEquals(7, result.departures.size) // 05:00..06:00 every 10 min
        assertEquals(listOf("s1", "s2"), result.stops.map { it.id })
    }

    @Test
    fun `directions go from line origin to the tapped stop`() = runTest {
        val steps = repo(now = 300).directionsTo("L1", "s2")
        assertEquals(3, steps.size)
        assertEquals(DirectionStep.Board("34", "Kadıköy"), steps[0])
        assertEquals(DirectionStep.Ride(1), steps[1])
        assertEquals(DirectionStep.Alight("Acıbadem"), steps[2])
    }

    @Test
    fun `tapping the origin stop yields no directions`() = runTest {
        assertTrue(repo(now = 300).directionsTo("L1", "s1").isEmpty())
    }

    @Test
    fun `search matches stop names and line codes`() = runTest {
        val r = repo(now = 300)
        assertTrue(r.search("Acı").stops.any { it.name == "Acıbadem" })
        assertTrue(r.search("34").lines.any { it.code == "34" })
        assertFalse(r.search("").let { it.lines.isNotEmpty() || it.stops.isNotEmpty() })
    }
}

/** In-memory BusDao for pure-JVM logic tests. */
private class FakeBusDao(
    private val stops: List<BusStopEntity>,
    private val lines: List<BusLineEntity>,
    private val routeStops: List<BusRouteStopEntity>,
) : BusDao {
    override suspend fun lineCount() = lines.size
    override suspend fun searchStops(q: String) =
        stops.filter { it.name.contains(q, true) || it.code.contains(q, true) }
    override suspend fun searchLines(q: String) =
        lines.filter { it.code.contains(q, true) || it.originName.contains(q, true) || it.destinationName.contains(q, true) }
    override suspend fun stopById(id: String) = stops.firstOrNull { it.id == id }
    override suspend fun lineById(id: String) = lines.firstOrNull { it.id == id }
    override suspend fun stopsByIds(ids: List<String>) = stops.filter { it.id in ids }
    override suspend fun routeStopsForLine(lineId: String) =
        routeStops.filter { it.lineId == lineId }.sortedBy { it.seq }
    override suspend fun routeStopsAtStop(stopId: String) = routeStops.filter { it.stopId == stopId }
    override suspend fun linesForStop(stopId: String): List<BusLineEntity> {
        val ids = routeStops.filter { it.stopId == stopId }.map { it.lineId }.toSet()
        return lines.filter { it.id in ids }.sortedBy { it.code }
    }
    override suspend fun insertStops(items: List<BusStopEntity>) = Unit
    override suspend fun insertLines(items: List<BusLineEntity>) = Unit
    override suspend fun insertRouteStops(items: List<BusRouteStopEntity>) = Unit
}
