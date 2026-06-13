package com.transitline.istanbul.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.transitline.istanbul.data.local.entity.BusLineEntity
import com.transitline.istanbul.data.local.entity.BusRouteStopEntity
import com.transitline.istanbul.data.local.entity.BusStopEntity

@Dao
interface BusDao {

    @Query("SELECT COUNT(*) FROM bus_line")
    suspend fun lineCount(): Int

    @Query(
        "SELECT * FROM bus_stop WHERE name LIKE '%' || :q || '%' OR code LIKE '%' || :q || '%' " +
            "ORDER BY name LIMIT 30",
    )
    suspend fun searchStops(q: String): List<BusStopEntity>

    @Query(
        "SELECT * FROM bus_line WHERE code LIKE '%' || :q || '%' OR originName LIKE '%' || :q || '%' " +
            "OR destinationName LIKE '%' || :q || '%' ORDER BY code LIMIT 30",
    )
    suspend fun searchLines(q: String): List<BusLineEntity>

    @Query("SELECT * FROM bus_stop WHERE id = :id")
    suspend fun stopById(id: String): BusStopEntity?

    @Query("SELECT * FROM bus_line WHERE id = :id")
    suspend fun lineById(id: String): BusLineEntity?

    @Query("SELECT * FROM bus_stop WHERE id IN (:ids)")
    suspend fun stopsByIds(ids: List<String>): List<BusStopEntity>

    /** Ordered route of a line. */
    @Query("SELECT * FROM bus_route_stop WHERE lineId = :lineId ORDER BY seq")
    suspend fun routeStopsForLine(lineId: String): List<BusRouteStopEntity>

    /** Every route-stop row that touches a given physical stop. */
    @Query("SELECT * FROM bus_route_stop WHERE stopId = :stopId")
    suspend fun routeStopsAtStop(stopId: String): List<BusRouteStopEntity>

    /** Lines that pass through a stop, for the horizontal chip row. */
    @Query(
        "SELECT l.* FROM bus_line l INNER JOIN bus_route_stop rs ON rs.lineId = l.id " +
            "WHERE rs.stopId = :stopId ORDER BY l.code",
    )
    suspend fun linesForStop(stopId: String): List<BusLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(items: List<BusStopEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(items: List<BusLineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteStops(items: List<BusRouteStopEntity>)
}
