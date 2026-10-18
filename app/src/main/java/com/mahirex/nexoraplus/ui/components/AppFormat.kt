package com.mahirex.nexoraplus.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.core.util.PersianDigits
import com.mahirex.nexoraplus.core.util.TimeFormat

/**
 * Locale/format context shared app-wide:
 * calendar type (Jalali/Gregorian), 24h toggle, Persian digits & bidi safety.
 */
data class AppFormat(
    val calendarType: Int = 0,
    val time24: Boolean = true,
    val persianDigits: Boolean = true,
    val languageTag: String = ""
) {
    private val d = { s: String ->
        val withDigits = if (persianDigits) PersianDigits.toFa(s) else s
        com.mahirex.nexoraplus.core.util.Bidi.safe(withDigits)
    }

    fun date(epochDay: Long, withWeekday: Boolean = true): String =
        if (withWeekday) CalendarConverter.formatFullDate(epochDay, calendarType == 0, persianLocale(), d)
        else CalendarConverter.formatShortDate(epochDay, calendarType == 0, persianLocale(), d)

    fun dateOfJalali(j: CalendarConverter.JalaliDate, withWeekday: Boolean = true): String {
        val base = if (withWeekday) {
            "${CalendarConverter.weekdayName(j.toLocalDate().toEpochDay(), persianLocale())} ${j.day} " +
                "${CalendarConverter.jalaliMonthName(j.month, persianLocale())} ${j.year}"
        } else {
            "${j.day} ${CalendarConverter.jalaliMonthName(j.month, persianLocale())} ${j.year}"
        }
        return d(base)
    }

    fun time(minutes: Int): String = TimeFormat.label(minutes, time24, persianDigits)

    fun timeRange(start: Int, end: Int): String = "${time(start)} – ${time(end)}"

    fun num(n: Int): String = if (persianDigits) PersianDigits.num(n, true) else n.toString()

    fun num(n: Long): String = if (persianDigits) PersianDigits.num(n, true) else n.toString()

    fun typed(s: String): String = d(s)

    fun persianLocale(): Boolean = persianDigits

    companion object {
        fun isFa(languageTag: String): Boolean {
            return if (languageTag.isEmpty()) {
                val device = java.util.Locale.getDefault().language
                device == "fa" || device == "ur"
            } else languageTag.substringBefore('-').lowercase() == "fa"
        }
    }
}

val LocalAppFormat = staticCompositionLocalOf { AppFormat() }
