package com.transitline.istanbul

import android.app.Application
import android.content.Context
import com.transitline.istanbul.core.util.LocaleUtil
import com.transitline.istanbul.data.datastore.SettingsRepository
import com.transitline.istanbul.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TransitLineApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Seed the offline network once, off the main thread.
        appScope.launch { container.seeder.seedIfNeeded() }
    }

    override fun attachBaseContext(base: Context) {
        val tag = SettingsRepository(base).currentBlocking().language.tag
        super.attachBaseContext(if (tag == null) base else LocaleUtil.wrap(base, tag))
    }
}
