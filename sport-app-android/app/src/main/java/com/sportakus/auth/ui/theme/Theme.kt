package com.sportakus.auth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Color

private val DarkForeground = Color(0xFFF2F5EE)
private val DarkBackgroundColor = Color(0xFF101510)
private val DarkSurfaceColor = Color(0xFF1B2219)
private val DarkFieldColor = Color(0xFF252E22)
private val BrandDark = Color(0xFF507A12)

private val DarkTypography: Typography = Typography().let { t ->
    t.copy(
        displayLarge = t.displayLarge.copy(color = DarkForeground),
        displayMedium = t.displayMedium.copy(color = DarkForeground),
        displaySmall = t.displaySmall.copy(color = DarkForeground),
        headlineLarge = t.headlineLarge.copy(color = DarkForeground),
        headlineMedium = t.headlineMedium.copy(color = DarkForeground),
        headlineSmall = t.headlineSmall.copy(color = DarkForeground),
        titleLarge = t.titleLarge.copy(color = DarkForeground),
        titleMedium = t.titleMedium.copy(color = DarkForeground),
        titleSmall = t.titleSmall.copy(color = DarkForeground),
        bodyLarge = t.bodyLarge.copy(color = DarkForeground),
        bodyMedium = t.bodyMedium.copy(color = DarkForeground),
        bodySmall = t.bodySmall.copy(color = DarkForeground),
        labelLarge = t.labelLarge.copy(color = DarkForeground),
        labelMedium = t.labelMedium.copy(color = DarkForeground),
        labelSmall = t.labelSmall.copy(color = DarkForeground)
    )
}

private val SportakusDarkColors = darkColorScheme(
    primary = BrandDark,
    onPrimary = DarkForeground,
    primaryContainer = Color(0xFF36551D),
    onPrimaryContainer = DarkForeground,
    inversePrimary = Color(0xFF9BCB54),
    secondary = DarkFieldColor,
    onSecondary = DarkForeground,
    secondaryContainer = Color(0xFF303E28),
    onSecondaryContainer = DarkForeground,
    tertiary = Color(0xFF303E28),
    onTertiary = DarkForeground,
    tertiaryContainer = Color(0xFF303E28),
    onTertiaryContainer = DarkForeground,
    background = DarkBackgroundColor,
    onBackground = DarkForeground,
    surface = DarkSurfaceColor,
    onSurface = DarkForeground,
    surfaceVariant = DarkFieldColor,
    onSurfaceVariant = DarkForeground,
    surfaceTint = Color(0xFF9BCB54),
    inverseSurface = DarkFieldColor,
    inverseOnSurface = DarkForeground,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF4A2826),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF45523D),
    outlineVariant = Color(0xFF364130),
    scrim = Color(0x99000000)
)

/** The dark-only edition always uses a single dark color scheme. */
@Composable
fun SportakusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SportakusDarkColors,
        typography = DarkTypography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides DarkForeground,
            LocalTextStyle provides LocalTextStyle.current.copy(color = DarkForeground)
        ) {
            content()
        }
    }
}
