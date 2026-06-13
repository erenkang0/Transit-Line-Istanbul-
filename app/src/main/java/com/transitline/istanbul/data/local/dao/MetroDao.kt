package com.transitline.istanbul.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.transitline.istanbul.data.local.entity.MetroLineEntity
import com.transitline.istanbul.data.local.entity.MetroLineStationCrossRef
import com.transitline.istanbul.data.local.entity.MetroStationEntity
import com.transitline.istanbul.data.local.entity.MetroWalkTransferEntity

@Dao
interface MetroDao {

    @Query("SELECT COUNT(*) FROM metro_line")
    suspend fun lineCount(): Int

    @Query("SELECT * FROM metro_line ORDER BY orderIndex")
    suspend fun lines(): List<MetroLineEntity>

    @Query("SELECT * FROM metro_station")
    suspend fun stations(): List<MetroStationEntity>

    @Query("SELECT * FROM metro_line_station ORDER BY lineId, seq")
    suspend fun lineStations(): List<MetroLineStationCrossRef>

    @Query("SELECT * FROM metro_walk_transfer")
    suspend fun walkTransfers(): List<MetroWalkTransferEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(items: List<MetroLineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(items: List<MetroStationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineStations(items: List<MetroLineStationCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalkTransfers(items: List<MetroWalkTransferEntity>)
}
