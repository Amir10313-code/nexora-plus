package com.mahirex.nexoraplus.core.util

/**
 * Mixed Persian/English text safety net.
 * All UI text goes through [safe] before being rendered so strings that mix
 * Persian and Latin/numbers never lose their base direction.
 */
object Bidi {

    private const val RLM = '\u200F'
    private const val LRM = '\u200E'

    fun safe(text: String): String {
        if (text.isEmpty()) return text
        val hasPersian = text.any { it in '\u0600'..'\u06FF' || it in '\uFB50'..'\uFEFF' }
        val firstStrong = text.firstOrNull { it.isLetter() } ?: return text
        val firstIsRtl = firstStrong in '\u0600'..'\u06FF'
        return when {
            hasPersian && !firstIsRtl -> "$RLM$text"
            !hasPersian && firstIsRtl -> "$LRM$text"
            else -> text
        }
    }
}
