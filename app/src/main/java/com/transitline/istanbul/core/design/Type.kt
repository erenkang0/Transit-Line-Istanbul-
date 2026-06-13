package com.transitline.istanbul.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle

/**
 * Built on the Material 3 type-scale tokens. We keep the default metrics (which
 * are the type-scale tokens themselves) and only normalize line-height behavior
 * so multi-line Turkish strings stay even. Per-user scaling is applied through
 * Density.fontScale in the theme, not by editing these styles.
 */
private val EvenLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun TextStyle.tuned(): TextStyle = copy(
    fontFamily = FontFamily.Default,
    lineHeightStyle = EvenLineHeight,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

val TransitTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.tuned(),
        displayMedium = displayMedium.tuned(),
        displaySmall = displaySmall.tuned(),
        headlineLarge = headlineLarge.tuned(),
        headlineMedium = headlineMedium.tuned(),
        headlineSmall = headlineSmall.tuned(),
        titleLarge = titleLarge.tuned(),
        titleMedium = titleMedium.tuned(),
        titleSmall = titleSmall.tuned(),
        bodyLarge = bodyLarge.tuned(),
        bodyMedium = bodyMedium.tuned(),
        bodySmall = bodySmall.tuned(),
        labelLarge = labelLarge.tuned(),
        labelMedium = labelMedium.tuned(),
        labelSmall = labelSmall.tuned(),
    )
}
