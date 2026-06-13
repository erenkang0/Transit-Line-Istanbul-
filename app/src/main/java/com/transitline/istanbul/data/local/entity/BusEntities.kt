package com.transitline.istanbul.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bus_stop")
data class BusStopEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
)

@Entity(tableName = "bus_line")
data class BusLineEntity(
    @PrimaryKey val id: String,
    val code: String,
    val originName: String,
    val destinationName: String,
    /** Schedule encoded compactly: minutes-of-day of first/last trip + headway. */
    val firstDepartureMin: Int,
    val lastDepartureMin: Int,
    val headwayMin: Int,
)

@Entity(
    tableName = "bus_route_stop",
    primaryKeys = ["lineId", "stopId"],
    indices = [Index("lineId"), Index("stopId")],
)
data class BusRouteStopEntity(
    val lineId: String,
    val stopId: String,
    val seq: Int,
    /** Travel time in minutes from the line origin to this stop. */
    val offsetMin: Int,
)
