package com.mahirex.nexoraplus.core.util

import com.mahirex.nexoraplus.core.calendar.CalendarConverter

/** Time formatting shared by every screen (24h / 12h + Persian digits). */
object TimeFormat {

    fun label(minutes: Int, is24: Boolean, persian: Boolean): String {
        val m = CalendarConverter.floorMod(minutes.toLong(), 1440)
        val h = m / 60
        val min = m % 60
        val mm = if (min < 10) "0$min" else min.toString()
        return if (is24) {
            val hh = if (h < 10) "0$h" else h.toString()
            PersianDigits.pair(hh, mm, persian)
        } else {
            val am = h < 12
            var h12 = h % 12
            if (h12 == 0) h12 = 12
            val base = PersianDigits.pair(h12.toString(), mm, persian)
            if (am) "$base صبح" else "$base عصر"
        }
    }

    /** Returns hour & minute parsed from a typed string like "9:30". */
    fun parseTyped(text: String): Pair<Int, Int>? {
        val t = PersianDigits.toLatin(text.trim())
        val parts = t.split(':', '،', ' ')
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].padEnd(2, '0').take(2).toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h to m
    }

    fun dialLabel(minutes: Int, is24: Boolean, persian: Boolean) = label(minutes, is24, persian)
}

private fun PersianDigits.pair(a: String, b: String, persian: Boolean): String =
    (if (persian) toFa("$a:$b") else "$a:$b")
