package com.mahirex.nexoraplus.ui.theme

import androidx.compose.ui.graphics.Color

// ── Nexora+ enterprise palette ──────────────────────────────────────────────
// Primary purple #5B3FE0 (light) / #7C5CFF (dark)
// Secondary cyan  #0891B2 (light) / #22D3EE (dark)
// Tertiary emerald #059669 (light) / #34D399 (dark)
// Background      #F4F6FB (light) / #0B1020 (dark)

val NexoraPurple = Color(0xFF5B3FE0)
val NexoraPurpleDark = Color(0xFF7C5CFF)
val NexoraCyan = Color(0xFF0891B2)
val NexoraCyanDark = Color(0xFF22D3EE)
val NexoraEmerald = Color(0xFF059669)
val NexoraEmeraldDark = Color(0xFF34D399)

val NexoraGradientStart = NexoraPurple
val NexoraGradientEnd = NexoraCyan

// Account color options
val AccountColors = listOf(
    Color(0xFF5B3FE0),
    Color(0xFF0891B2),
    Color(0xFF059669),
    Color(0xFFDB2777),
    Color(0xFFD97706),
    Color(0xFFDC2626),
    Color(0xFF4F46E5),
    Color(0xFF0D9488)
)

// Task/event coloring options
val PaletteColors = listOf(
    Color(0xFF5B3FE0),
    Color(0xFF7C5CFF),
    Color(0xFF0891B2),
    Color(0xFF22D3EE),
    Color(0xFF059669),
    Color(0xFF34D399),
    Color(0xFFDB2777),
    Color(0xFFEC4899),
    Color(0xFFD97706),
    Color(0xFFF59E0B),
    Color(0xFFDC2626),
    Color(0xFFEF4444)
)

fun colorFromHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    NexoraPurple
}

fun colorToHex(color: Color): String = String.format("#%06X", 0xFFFFFF and color.toArgb())

private fun Color.toArgb(): Int {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}
