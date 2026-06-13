package com.transitline.istanbul.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "metro_line")
data class MetroLineEntity(
    @PrimaryKey val id: String,
    val code: String,
    val nameTr: String,
    val nameEn: String,
    /** Official line color as #AARRGGBB. */
    val colorHex: String,
    val orderIndex: Int,
)

@Entity(tableName = "metro_station")
data class MetroStationEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Normalized schematic coordinates in 0..1, used only for drawing. */
    val x: Float,
    val y: Float,
    /** Real-world coordinates, used only to highlight the nearest stop via GPS. */
    val lat: Double? = null,
    val lon: Double? = null,
)

@Entity(
    tableName = "metro_line_station",
    primaryKeys = ["lineId", "stationId"],
    indices = [Index("lineId"), Index("stationId")],
)
data class MetroLineStationCrossRef(
    val lineId: String,
    val stationId: String,
    val seq: Int,
)

@Entity(tableName = "metro_walk_transfer")
data class MetroWalkTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromStationId: String,
    val toStationId: String,
)
