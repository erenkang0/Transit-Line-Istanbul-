package com.transitline.istanbul

import com.transitline.istanbul.data.local.dao.MetroDao
import com.transitline.istanbul.data.local.entity.MetroLineEntity
import com.transitline.istanbul.data.local.entity.MetroLineStationCrossRef
import com.transitline.istanbul.data.local.entity.MetroStationEntity
import com.transitline.istanbul.data.local.entity.MetroWalkTransferEntity
import com.transitline.istanbul.data.repository.MetroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetroRepositoryTest {

    private val stations = listOf(
        MetroStationEntity("a", "Alpha", 0.1f, 0.1f, lat = 41.00, lon = 29.00),
        MetroStationEntity("b", "Beta", 0.5f, 0.5f, lat = 41.01, lon = 29.01),
        MetroStationEntity("c", "Gamma", 0.9f, 0.9f, lat = 41.02, lon = 29.02),
    )
    private val lines = listOf(
        MetroLineEntity("M2", "M2", "M2 tr", "M2 en", "#FF009A44", 0),
        MetroLineEntity("M4", "M4", "M4 tr", "M4 en", "#FFE6007E", 1),
    )
    private val crossRefs = listOf(
        MetroLineStationCrossRef("M2", "a", 0),
        MetroLineStationCrossRef("M2", "b", 1),
        MetroLineStationCrossRef("M4", "b", 0),
        MetroLineStationCrossRef("M4", "c", 1),
    )

    private fun repo() = MetroRepository(FakeMetroDao(lines, stations, crossRefs, emptyList()))

    @Test
    fun `network maps lines with ordered stations and parsed official color`() = runTest {
        val network = repo().networkFlow().first()
        assertEquals(2, network.lines.size)
        val m2 = network.lines.first { it.code == "M2" }
        assertEquals(listOf("a", "b"), m2.stationIds)
        assertEquals(0xFF009A44L, m2.colorArgb)
    }

    @Test
    fun `a station on two lines is an interchange`() = runTest {
        val network = repo().networkFlow().first()
        assertTrue(network.isInterchange("b"))
        assertFalse(network.isInterchange("a"))
        assertEquals(2, network.linesFor("b").size)
    }

    @Test
    fun `nearest station picks the closest coordinate`() = runTest {
        val r = repo()
        assertEquals("a", r.nearestStation(41.001, 29.001)?.id)
        assertEquals("c", r.nearestStation(41.019, 29.019)?.id)
    }
}

/** In-memory MetroDao backed by static flows for pure-JVM logic tests. */
private class FakeMetroDao(
    private val lineRows: List<MetroLineEntity>,
    private val stationRows: List<MetroStationEntity>,
    private val crossRefRows: List<MetroLineStationCrossRef>,
    private val walkRows: List<MetroWalkTransferEntity>,
) : MetroDao {
    override suspend fun lineCount() = lineRows.size
    override fun linesFlow(): Flow<List<MetroLineEntity>> = flowOf(lineRows)
    override fun stationsFlow(): Flow<List<MetroStationEntity>> = flowOf(stationRows)
    override fun lineStationsFlow(): Flow<List<MetroLineStationCrossRef>> = flowOf(crossRefRows)
    override fun walkTransfersFlow(): Flow<List<MetroWalkTransferEntity>> = flowOf(walkRows)
    override suspend fun stations() = stationRows
    override suspend fun insertLines(items: List<MetroLineEntity>) = Unit
    override suspend fun insertStations(items: List<MetroStationEntity>) = Unit
    override suspend fun insertLineStations(items: List<MetroLineStationCrossRef>) = Unit
    override suspend fun insertWalkTransfers(items: List<MetroWalkTransferEntity>) = Unit
}
