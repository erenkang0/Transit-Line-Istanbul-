package com.transitline.istanbul.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.transitline.istanbul.data.local.dao.BusDao
import com.transitline.istanbul.data.local.dao.FavoriteDao
import com.transitline.istanbul.data.local.dao.MetroDao
import com.transitline.istanbul.data.local.entity.BusLineEntity
import com.transitline.istanbul.data.local.entity.BusRouteStopEntity
import com.transitline.istanbul.data.local.entity.BusStopEntity
import com.transitline.istanbul.data.local.entity.FavoriteEntity
import com.transitline.istanbul.data.local.entity.MetroLineEntity
import com.transitline.istanbul.data.local.entity.MetroLineStationCrossRef
import com.transitline.istanbul.data.local.entity.MetroStationEntity
import com.transitline.istanbul.data.local.entity.MetroWalkTransferEntity

@Database(
    entities = [
        MetroLineEntity::class,
        MetroStationEntity::class,
        MetroLineStationCrossRef::class,
        MetroWalkTransferEntity::class,
        BusStopEntity::class,
        BusLineEntity::class,
        BusRouteStopEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TransitDatabase : RoomDatabase() {
    abstract fun metroDao(): MetroDao
    abstract fun busDao(): BusDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val NAME = "transitline.db"
    }
}
