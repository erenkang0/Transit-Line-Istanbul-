package com.transitline.istanbul.di

import android.content.Context
import androidx.room.Room
import com.transitline.istanbul.core.connectivity.ConnectivityObserver
import com.transitline.istanbul.core.location.LocationProvider
import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.data.local.TransitDatabase
import com.transitline.istanbul.data.local.seed.DatabaseSeeder
import com.transitline.istanbul.data.remote.LiveDataSource
import com.transitline.istanbul.data.remote.OfflineOnlyLiveDataSource
import com.transitline.istanbul.data.repository.BackupManager
import com.transitline.istanbul.data.repository.BusRepository
import com.transitline.istanbul.data.repository.FavoritesRepository
import com.transitline.istanbul.data.repository.MetroRepository
import kotlinx.serialization.json.Json

/**
 * Manual dependency container held by the Application. Chosen over a DI framework
 * to keep the build simple and the graph obvious — the app is small and every
 * dependency is a plain singleton.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val database: TransitDatabase = Room.databaseBuilder(
        appContext,
        TransitDatabase::class.java,
        TransitDatabase.NAME,
    ).build()

    val settingsRepository = SettingsRepository(appContext)
    val connectivityObserver = ConnectivityObserver(appContext)
    val locationProvider = LocationProvider(appContext)

    /** Swap this for a real İETT/GTFS-RT source once the integration is approved. */
    private val liveDataSource: LiveDataSource = OfflineOnlyLiveDataSource()

    val metroRepository = MetroRepository(database.metroDao())
    val busRepository = BusRepository(database.busDao(), liveDataSource)
    val favoritesRepository = FavoritesRepository(database.favoriteDao())
    val backupManager = BackupManager(settingsRepository, database.favoriteDao(), json)
    val seeder = DatabaseSeeder(appContext, database, json)
}
