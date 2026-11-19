package com.sportakus.auth.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Stable palette for the light-only V2 application.
 * These values intentionally do not depend on inherited Material content colors
 * or runtime/system theme state, so text remains readable on every screen.
 */
@Composable
fun sportakusTextColor(): Color = LightText

@Composable
fun sportakusSecondaryTextColor(): Color = LightSecondaryText

/** Selected questionnaire tiles match the theme's selected/brand container. */
@Composable
fun sportakusSelectedOptionBackground(): Color = Color(0xFFEFF6DD)

@Composable
fun sportakusSelectedOptionTextColor(): Color = LightText

/** Editable fields use the fixed white field surface in the light-only V2. */
@Composable
fun sportakusInputBackground(): Color = LightField

@Composable
fun sportakusInputTextColor(): Color = LightText

/** Foreground for the solid primary action button. */
@Composable
fun sportakusPrimaryActionTextColor(): Color = Color.White
