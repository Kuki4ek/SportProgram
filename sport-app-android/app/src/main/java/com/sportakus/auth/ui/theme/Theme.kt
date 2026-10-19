package com.sportakus.auth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SportakusGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4F2C7),
    onPrimaryContainer = Color(0xFF20320B),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightField,
    onSurfaceVariant = LightSecondaryText,
    outline = LightOutline,
    outlineVariant = LightOutline,
    secondary = LightSecondaryText
)

private val DarkColors = darkColorScheme(
    primary = SportakusGreenDark,
    onPrimary = Color(0xFF17220B),
    primaryContainer = Color(0xFF304810),
    onPrimaryContainer = Color(0xFFE8F6D0),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkField,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    secondary = DarkSecondaryText
)

@Composable
fun SportakusTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
