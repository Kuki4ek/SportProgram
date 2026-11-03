package com.sportakus.auth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brand = Color(0xFF496900)

private val DarkForeground = Color(0xFFF2F5EE)
private val DarkMuted = Color(0xFFB8C5AE)
private val DarkBackground = Color(0xFF101510)
private val DarkSurface = Color(0xFF1B2219)
private val DarkField = Color(0xFF252E22)
private val DarkOutline = Color(0xFF45523D)
private val DarkOutlineVariant = Color(0xFF3E4937)

private val LightForeground = Color.Black
private val LightMuted = Color(0xFF53594A)
private val LightThemeBackground = Color(0xFFF5F7F1)
private val LightThemeSurface = Color(0xFFFFFFFF)
private val LightThemeField = Color(0xFFFFFFFF)
private val LightThemeOutline = Color(0xFFD3DACB)
private val LightThemeOutlineVariant = Color(0xFFE3E8DD)

private fun typographyFor(foreground: Color): Typography = Typography().let { t ->
    t.copy(
        displayLarge = t.displayLarge.copy(color = foreground),
        displayMedium = t.displayMedium.copy(color = foreground),
        displaySmall = t.displaySmall.copy(color = foreground),
        headlineLarge = t.headlineLarge.copy(color = foreground),
        headlineMedium = t.headlineMedium.copy(color = foreground),
        headlineSmall = t.headlineSmall.copy(color = foreground),
        titleLarge = t.titleLarge.copy(color = foreground),
        titleMedium = t.titleMedium.copy(color = foreground),
        titleSmall = t.titleSmall.copy(color = foreground),
        bodyLarge = t.bodyLarge.copy(color = foreground),
        bodyMedium = t.bodyMedium.copy(color = foreground),
        bodySmall = t.bodySmall.copy(color = foreground),
        labelLarge = t.labelLarge.copy(color = foreground),
        labelMedium = t.labelMedium.copy(color = foreground),
        labelSmall = t.labelSmall.copy(color = foreground)
    )
}

private val SportakusDarkColors = darkColorScheme(
    primary = Brand,
    onPrimary = Color(0xFFF2F5EE),
    primaryContainer = Color(0xFF36551D),
    onPrimaryContainer = DarkForeground,
    inversePrimary = Color(0xFF9BCB54),
    secondary = DarkField,
    onSecondary = DarkForeground,
    secondaryContainer = Color(0xFF303E28),
    onSecondaryContainer = DarkForeground,
    tertiary = Color(0xFF303E28),
    onTertiary = DarkForeground,
    tertiaryContainer = Color(0xFF303E28),
    onTertiaryContainer = DarkForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkSurface,
    onSurface = DarkForeground,
    surfaceVariant = DarkField,
    onSurfaceVariant = DarkMuted,
    surfaceTint = Color(0xFF9BCB54),
    inverseSurface = DarkField,
    inverseOnSurface = DarkForeground,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF4A2826),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = Color(0x99000000)
)

private val SportakusLightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6DD),
    onPrimaryContainer = Color(0xFF1E2A12),
    inversePrimary = Color(0xFFB8DC83),
    secondary = Color(0xFFE8EDE1),
    onSecondary = LightForeground,
    secondaryContainer = Color(0xFFDDE8CF),
    onSecondaryContainer = LightForeground,
    tertiary = Color(0xFFE1EAD5),
    onTertiary = LightForeground,
    tertiaryContainer = Color(0xFFD7E5C6),
    onTertiaryContainer = LightForeground,
    background = LightThemeBackground,
    onBackground = LightForeground,
    surface = LightThemeSurface,
    onSurface = LightForeground,
    surfaceVariant = LightThemeField,
    onSurfaceVariant = LightMuted,
    surfaceTint = Brand,
    inverseSurface = Color(0xFF2B3225),
    inverseOnSurface = Color(0xFFF3F5EF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = LightThemeOutline,
    outlineVariant = LightThemeOutlineVariant,
    scrim = Color(0x66000000)
)

/** V2 uses one consistent light color scheme throughout the application. */
@Composable
fun SportakusTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // V2 is intentionally light-only. The parameter remains for source compatibility
    // with screen previews that still pass an explicit Boolean.
    val scheme = SportakusLightColors
    val typography = typographyFor(LightForeground)

    // MaterialTheme is the only source of truth for semantic UI colors.
    // Text, input backgrounds, and selected options read their colors from this
    // exact scheme, so they cannot become desynchronized from the visible theme.
    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
        content = content
    )
}
