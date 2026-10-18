package com.mahirex.nexoraplus.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B3FE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6DFFF),
    onPrimaryContainer = Color(0xFF1D1064),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF3FB),
    onSecondaryContainer = Color(0xFF063B49),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFF5E6),
    onTertiaryContainer = Color(0xFF02422C),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF191C24),
    surface = Color.White,
    onSurface = Color(0xFF191C24),
    surfaceVariant = Color(0xFFE9EBF4),
    onSurfaceVariant = Color(0xFF565B6B),
    outline = Color(0xFFC7CAD6),
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C5CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3A2A96),
    onPrimaryContainer = Color(0xFFE4DBFF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF052E38),
    secondaryContainer = Color(0xFF0B4A5C),
    onSecondaryContainer = Color(0xFFCFF3FB),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF03301F),
    tertiaryContainer = Color(0xFF0A4D36),
    onTertiaryContainer = Color(0xFFCFF5E6),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE6E8F2),
    surface = Color(0xFF131A31),
    onSurface = Color(0xFFE6E8F2),
    surfaceVariant = Color(0xFF232B4A),
    onSurfaceVariant = Color(0xFFA9AFC6),
    outline = Color(0xFF3A4266),
    error = Color(0xFFF87171),
    onError = Color(0xFF3B0A0A)
)

private fun lerpScheme(a: ColorScheme, b: ColorScheme, t: Float): ColorScheme = a.copy(
    primary = lerp(a.primary, b.primary, t),
    onPrimary = lerp(a.onPrimary, b.onPrimary, t),
    primaryContainer = lerp(a.primaryContainer, b.primaryContainer, t),
    onPrimaryContainer = lerp(a.onPrimaryContainer, b.onPrimaryContainer, t),
    inversePrimary = lerp(a.inversePrimary, b.inversePrimary, t),
    secondary = lerp(a.secondary, b.secondary, t),
    onSecondary = lerp(a.onSecondary, b.onSecondary, t),
    secondaryContainer = lerp(a.secondaryContainer, b.secondaryContainer, t),
    onSecondaryContainer = lerp(a.onSecondaryContainer, b.onSecondaryContainer, t),
    tertiary = lerp(a.tertiary, b.tertiary, t),
    onTertiary = lerp(a.onTertiary, b.onTertiary, t),
    tertiaryContainer = lerp(a.tertiaryContainer, b.tertiaryContainer, t),
    onTertiaryContainer = lerp(a.onTertiaryContainer, b.onTertiaryContainer, t),
    background = lerp(a.background, b.background, t),
    onBackground = lerp(a.onBackground, b.onBackground, t),
    surface = lerp(a.surface, b.surface, t),
    onSurface = lerp(a.onSurface, b.onSurface, t),
    surfaceVariant = lerp(a.surfaceVariant, b.surfaceVariant, t),
    onSurfaceVariant = lerp(a.onSurfaceVariant, b.onSurfaceVariant, t),
    surfaceTint = lerp(a.surfaceTint, b.surfaceTint, t),
    inverseSurface = lerp(a.inverseSurface, b.inverseSurface, t),
    inverseOnSurface = lerp(a.inverseOnSurface, b.inverseOnSurface, t),
    error = lerp(a.error, b.error, t),
    onError = lerp(a.onError, b.onError, t),
    errorContainer = lerp(a.errorContainer, b.errorContainer, t),
    onErrorContainer = lerp(a.onErrorContainer, b.onErrorContainer, t),
    outline = lerp(a.outline, b.outline, t),
    outlineVariant = lerp(a.outlineVariant, b.outlineVariant, t),
    scrim = lerp(a.scrim, b.scrim, t),
    // Container tokens MUST be lerped too — otherwise they keep their light-theme
    // values in dark mode (dialogs, sheets and menus render with light surfaces).
    surfaceDim = lerp(a.surfaceDim, b.surfaceDim, t),
    surfaceBright = lerp(a.surfaceBright, b.surfaceBright, t),
    surfaceContainerLowest = lerp(a.surfaceContainerLowest, b.surfaceContainerLowest, t),
    surfaceContainerLow = lerp(a.surfaceContainerLow, b.surfaceContainerLow, t),
    surfaceContainer = lerp(a.surfaceContainer, b.surfaceContainer, t),
    surfaceContainerHigh = lerp(a.surfaceContainerHigh, b.surfaceContainerHigh, t),
    surfaceContainerHighest = lerp(a.surfaceContainerHighest, b.surfaceContainerHighest, t)
)

/** themeMode: 0 = light, 1 = dark, 2 = follow system */
@Composable
fun NexoraTheme(
    themeMode: Int,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val targetDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> systemDark
    }
    val progress = animateFloatAsState(
        targetValue = if (targetDark) 1f else 0f,
        animationSpec = tween(350),
        label = "themeProgress"
    )
    val scheme = lerpScheme(LightColors, DarkColors, progress.value)
    MaterialTheme(
        colorScheme = scheme,
        typography = NexoraTypography,
        shapes = NexoraShapes,
        content = content
    )
}
