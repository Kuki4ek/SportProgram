package com.sportakus.auth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for semantic UI colors.
 * All of these values come from the exact Material color scheme rendered by
 * SportakusTheme, so text, inputs, and selected options cannot use a different
 * theme flag from their backgrounds.
 */
@Composable
fun sportakusTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
fun sportakusSecondaryTextColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

/** Selected questionnaire tiles match the theme's selected/brand container. */
@Composable
fun sportakusSelectedOptionBackground(): Color = MaterialTheme.colorScheme.primaryContainer

@Composable
fun sportakusSelectedOptionTextColor(): Color = MaterialTheme.colorScheme.onPrimaryContainer

/** Editable fields use the surface-variant role in both themes. */
@Composable
fun sportakusInputBackground(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun sportakusInputTextColor(): Color = MaterialTheme.colorScheme.onSurface

/** Foreground for the solid primary action button. */
@Composable
fun sportakusPrimaryActionTextColor(): Color = MaterialTheme.colorScheme.onPrimary
