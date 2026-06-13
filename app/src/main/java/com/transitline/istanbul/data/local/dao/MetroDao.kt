package com.transitline.istanbul.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.transitline.istanbul.data.local.entity.MetroLineEntity
import com.transitline.istanbul.data.local.entity.MetroLineStationCrossRef
import com.transitline.istanbul.data.local.entity.MetroStationEntity
import com.transitline.istanbul.data.local.entity.MetroWalkTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetroDao {

    @Query("SELECT COUNT(*) FROM metro_line")
    suspend fun lineCount(): Int

    // Flow queries so the map populates reactively the moment seeding finishes,
    // even if the screen subscribed while the database was still empty.
    @Query("SELECT * FROM metro_line ORDER BY orderIndex")
    fun linesFlow(): Flow<List<MetroLineEntity>>

    @Query("SELECT * FROM metro_station")
    fun stationsFlow(): Flow<List<MetroStationEntity>>

    @Query("SELECT * FROM metro_line_station ORDER BY lineId, seq")
    fun lineStationsFlow(): Flow<List<MetroLineStationCrossRef>>

    @Query("SELECT * FROM metro_walk_transfer")
    fun walkTransfersFlow(): Flow<List<MetroWalkTransferEntity>>

    /** One-shot snapshot used for the GPS nearest-station calculation. */
    @Query("SELECT * FROM metro_station")
    suspend fun stations(): List<MetroStationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(items: List<MetroLineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(items: List<MetroStationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineStations(items: List<MetroLineStationCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalkTransfers(items: List<MetroWalkTransferEntity>)
}
