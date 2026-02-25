package com.ifochka.m14n.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val M14nDarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = VioletOnPrimary,
    primaryContainer = VioletPrimaryContainer,
    onPrimaryContainer = VioletOnPrimaryContainer,
    secondary = LavenderSecondary,
    onSecondary = LavenderOnSecondary,
    secondaryContainer = LavenderSecondaryContainer,
    onSecondaryContainer = LavenderOnSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurface = OnDarkSurface,
    onSurfaceVariant = OnDarkSurfaceVariant,
    error = DarkError,
    onError = OnDarkError,
)

@Composable
fun M14nTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = M14nDarkColorScheme,
        content = content,
    )
}
