package com.mahirex.nexoraplus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Nexora typography.
 * Every style pins [TextDirection.Content] so mixed Persian/English strings
 * (e.g. «جلسه 25 دقیقه‌ای») always render with a clean, correct direction.
 */
private fun style(
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: androidx.compose.ui.unit.TextUnit = size * 1.45f,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp
) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    textDirection = TextDirection.Content
)

val NexoraTypography = Typography(
    displaySmall = style(36.sp, FontWeight.Bold),
    headlineMedium = style(28.sp, FontWeight.Bold),
    headlineSmall = style(24.sp, FontWeight.Bold),
    titleLarge = style(20.sp, FontWeight.Bold),
    titleMedium = style(17.sp, FontWeight.SemiBold),
    titleSmall = style(15.sp, FontWeight.SemiBold),
    bodyLarge = style(16.sp),
    bodyMedium = style(14.sp),
    bodySmall = style(12.sp),
    labelLarge = style(14.sp, FontWeight.Medium),
    labelMedium = style(12.sp, FontWeight.Medium),
    labelSmall = style(11.sp, FontWeight.Medium)
)
