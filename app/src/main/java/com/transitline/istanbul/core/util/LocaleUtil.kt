package com.transitline.istanbul.core.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies a chosen language by wrapping a base context with an overridden
 * locale. Works on every supported API level without pulling in AppCompat.
 */
object LocaleUtil {
    fun wrap(context: Context, languageTag: String): Context {
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
