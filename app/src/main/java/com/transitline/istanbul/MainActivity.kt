package com.transitline.istanbul

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.transitline.istanbul.core.design.TransitLineTheme
import com.transitline.istanbul.core.util.LocalePrefs
import com.transitline.istanbul.core.util.LocaleUtil
import com.transitline.istanbul.ui.home.AppRoot
import com.transitline.istanbul.ui.home.RootViewModel
import com.transitline.istanbul.ui.util.AppViewModelProvider

class MainActivity : ComponentActivity() {

    private val rootViewModel: RootViewModel by viewModels { AppViewModelProvider.Factory }

    override fun attachBaseContext(newBase: Context) {
        val tag = LocalePrefs.readTag(newBase)
        super.attachBaseContext(if (tag == null) newBase else LocaleUtil.wrap(newBase, tag))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var settingsLoaded = false
        splash.setKeepOnScreenCondition { !settingsLoaded }

        val appliedTag = LocalePrefs.readTag(this)

        setContent {
            val settings by rootViewModel.settings.collectAsStateWithLifecycle()
            val isOnline by rootViewModel.isOnline.collectAsStateWithLifecycle()
            val current = settings

            LaunchedEffect(current) { if (current != null) settingsLoaded = true }

            if (current != null) {
                // The locale is applied in attachBaseContext, so a language change
                // needs an Activity recreate to take effect.
                val applied = remember { appliedTag }
                LaunchedEffect(current.language) {
                    if (current.language.tag != applied) recreate()
                }

                TransitLineTheme(settings = current) {
                    AppRoot(settings = current, isOnline = isOnline)
                }
            }
        }
    }
}
