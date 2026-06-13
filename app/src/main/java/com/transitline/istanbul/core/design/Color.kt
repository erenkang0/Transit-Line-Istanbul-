package com.transitline.istanbul.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Matte, high-contrast Material 3 palette. No translucency, no glassmorphism —
 * surfaces are opaque and rely on tonal containers rather than blur for depth.
 */

private val BrandPrimaryLight = Color(0xFF1F6390)
private val BrandPrimaryDark = Color(0xFF8FCDFF)

val LightColors: ColorScheme = lightColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001E2E),
    secondary = Color(0xFF4F616E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E5F5),
    onSecondaryContainer = Color(0xFF0B1D29),
    tertiary = Color(0xFF63597C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF1F1635),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F0F7),
    surfaceContainer = Color(0xFFEDEAF1),
    surfaceContainerHigh = Color(0xFFE7E4EC),
    surfaceContainerHighest = Color(0xFFE1DEE6),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
)

val DarkColors: ColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFF00344C),
    primaryContainer = Color(0xFF004B6B),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFFB6C9D8),
    onSecondary = Color(0xFF21333E),
    secondaryContainer = Color(0xFF374955),
    onSecondaryContainer = Color(0xFFD2E5F5),
    tertiary = Color(0xFFCDC0E9),
    onTertiary = Color(0xFF342B4B),
    tertiaryContainer = Color(0xFF4B4163),
    onTertiaryContainer = Color(0xFFE9DDFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    surfaceContainerLowest = Color(0xFF0B0E11),
    surfaceContainerLow = Color(0xFF181C20),
    surfaceContainer = Color(0xFF1C2024),
    surfaceContainerHigh = Color(0xFF262A2E),
    surfaceContainerHighest = Color(0xFF313539),
    outline = Color(0xFF8B9297),
    outlineVariant = Color(0xFF41484D),
)

/**
 * True OLED black variant used by Power Saving mode: every background/surface
 * goes to pure #000000 so unlit pixels draw no power on OLED panels.
 */
fun ColorScheme.toPitchBlack(): ColorScheme = copy(
    background = Color.Black,
    onBackground = Color(0xFFEDEEF0),
    surface = Color.Black,
    onSurface = Color(0xFFEDEEF0),
    surfaceVariant = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFFBFC6CC),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0C0D),
    surfaceContainer = Color(0xFF101214),
    surfaceContainerHigh = Color(0xFF15181A),
    surfaceContainerHighest = Color(0xFF1B1E20),
    outline = Color(0xFF8A9197),
    outlineVariant = Color(0xFF2A2D30),
)

/** Bumps key foreground/outline contrast for the High Contrast preference. */
fun ColorScheme.toHighContrast(dark: Boolean): ColorScheme = if (dark) {
    copy(
        onSurface = Color(0xFFFFFFFF),
        onBackground = Color(0xFFFFFFFF),
        onSurfaceVariant = Color(0xFFE6ECF2),
        outline = Color(0xFFC9D0D6),
        outlineVariant = Color(0xFF7A8186),
    )
} else {
    copy(
        onSurface = Color(0xFF000000),
        onBackground = Color(0xFF000000),
        onSurfaceVariant = Color(0xFF1B2227),
        outline = Color(0xFF3A4146),
        outlineVariant = Color(0xFF5A6166),
    )
}
