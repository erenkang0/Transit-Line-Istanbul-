package com.transitline.istanbul.core.util

import android.content.Context

/**
 * The chosen language tag, mirrored into SharedPreferences. DataStore is async
 * and unsafe to read in attachBaseContext (before onCreate), but the locale must
 * be known there. This tiny synchronous store solves exactly that; DataStore
 * remains the source of truth for everything else.
 */
object LocalePrefs {
    private const val FILE = "locale_prefs"
    private const val KEY = "language_tag"

    fun readTag(context: Context): String? =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null)

    fun writeTag(context: Context, tag: String?) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, tag).apply()
    }
}
