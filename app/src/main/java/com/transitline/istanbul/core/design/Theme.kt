package com.transitline.istanbul.core.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.transitline.istanbul.domain.model.AppSettings

/**
 * Root theme. Resolves the color scheme from the user's preferences:
 *  - Power Saving forces a dark, true-OLED-black scheme and overrides system mode.
 *  - Material You (Android 12+) is honored when enabled; metro line colors are
 *    constants stored with the data, so they are never recolored by it.
 *  - High contrast and the text-size / easy-mode scaling are layered on top.
 */
@Composable
fun TransitLineTheme(
    settings: AppSettings,
    systemInDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val dark = settings.powerSaving || systemInDark
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    var scheme = when {
        settings.dynamicColor && supportsDynamic ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    if (settings.powerSaving) scheme = scheme.toPitchBlack()
    if (settings.highContrast) scheme = scheme.toHighContrast(dark)

    val base = LocalDensity.current
    val uiScale = settings.textSize.scale * if (settings.easyMode) 1.12f else 1f
    val scaledDensity = Density(
        density = base.density,
        fontScale = base.fontScale * uiScale,
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = scheme,
            typography = TransitTypography,
            shapes = TransitShapes,
            content = content,
        )
    }
}
