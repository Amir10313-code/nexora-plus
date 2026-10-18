package com.mahirex.nexoraplus.core.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Nexora+ calendar engine.
 * - Jalali (Solar Hijri) <-> Gregorian conversion (accurate 33-year cycle algorithm)
 * - Tabular Hijri (Lunar) conversion with an Iran-adjustment offset
 * - Locale-aware formatting helpers used across the whole app.
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    fun toLocalDate(): LocalDate = CalendarConverter.jalaliToGregorian(year, month, day)
}

object CalendarConverter {

    // ───────────────────────── Jalali <-> Gregorian ─────────────────────────

    private val G_DAY_OF_MONTH = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    private val JALALI_LEAP_REMAINDERS = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)

    fun isJalaliLeap(jy: Int): Boolean = (jy % 33) in JALALI_LEAP_REMAINDERS

    fun jalaliMonthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isJalaliLeap(jy)) 30 else 29
    }

    fun gregorianToJalali(date: LocalDate): JalaliDate =
        gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        var jy = if (gy <= 1600) 0 else 979
        val gy2 = if (gy <= 1600) gy - 621 else gy - 1600
        val gy3 = if (gm > 2) gy2 + 1 else gy2
        var days = 365 * gy2 + (gy3 + 3) / 4 - (gy3 + 99) / 100 + (gy3 + 399) / 400 -
            80 + gd + G_DAY_OF_MONTH[gm - 1]
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + (if (days < 186) days % 31 else (days - 186) % 30)
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
        var gy = if (jy <= 979) 621 else 1600
        val jy2 = if (jy <= 979) jy else jy - 979
        var days = 365L * jy2 + (jy2 / 33) * 8L + (jy2 % 33 + 3) / 4 + 78L + jd +
            (if (jm < 7) (jm - 1) * 31L else (jm - 7) * 30L + 186L)
        gy += (400 * (days / 146097)).toInt()
        days %= 146097
        if (days > 36524) {
            days--
            gy += (100 * (days / 36524)).toInt()
            days %= 36524
            if (days >= 365) days++
        }
        gy += (4 * (days / 1461)).toInt()
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365).toInt()
            days = (days - 1) % 365
        }
        var gd = (days + 1).toInt()
        val isLeapG = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val monthLens = intArrayOf(31, if (isLeapG) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 1
        while (gm <= 12 && gd > monthLens[gm - 1]) {
            gd -= monthLens[gm - 1]
            gm++
        }
        return LocalDate.of(gy, gm, gd)
    }

    fun jalaliToGregorian(j: JalaliDate): LocalDate = jalaliToGregorian(j.year, j.month, j.day)

    // ───────────────────────── Hijri (tabular, Iran-adjusted) ─────────────────────────

    /** Julian Day Number for a tabular Islamic date. */
    private fun hijriToJdn(hy: Int, hm: Int, hd: Int): Long =
        hd + ceil(29.5 * (hm - 1)).toLong() + 354L * (hy - 1) + (11 * hy + 3) / 30 + 1948440L - 1

    fun hijriToGregorian(hy: Int, hm: Int, hd: Int, adjustDays: Int = 1): LocalDate =
        LocalDate.ofEpochDay(hijriToJdn(hy, hm, hd) - 2440588L + adjustDays)

    fun hijriToJalali(hy: Int, hm: Int, hd: Int, adjustDays: Int = 1): JalaliDate =
        gregorianToJalali(hijriToGregorian(hy, hm, hd, adjustDays))

    fun gregorianToHijri(date: LocalDate, adjustDays: Int = 1): IntArray {
        val jdn = date.toEpochDay() + 2440588L - adjustDays
        val hy = ((30 * (jdn - 1948440) + 10646) / 10631).toInt()
        // compute month/day inside that year
        val yearStart = hijriToJdn(hy, 1, 1)
        var dayOfYear = (jdn - yearStart).toInt() + 1
        // Tabular leap years (355 days) end with a 30-day Dhū al-Ḥijja; assuming 29
        // would walk past month 12 and return the invalid month 13 (AIOOBE later).
        val dhuAlHijjaLen = if ((11 * hy + 14) % 30 < 11) 30 else 29
        var hm = 1
        while (hm <= 12) {
            val len = when {
                hm == 12 -> dhuAlHijjaLen
                hm % 2 == 1 -> 30
                else -> 29
            }
            if (dayOfYear <= len) break
            dayOfYear -= len
            hm++
        }
        if (hm > 12) {
            // jdn lands on the first day(s) of the next Hijri year — recompute against its start.
            val nextStart = hijriToJdn(hy + 1, 1, 1)
            return intArrayOf(hy + 1, 1, (jdn - nextStart).toInt() + 1)
        }
        return intArrayOf(hy, hm, dayOfYear)
    }

    /**
     * Returns all Jalali dates (month/day) inside the given Jalali year [jy]
     * that correspond to a lunar occasion defined by (hijriMonth, hijriDay).
     */
    fun hijriOccasionInJalaliYear(jy: Int, hijriMonth: Int, hijriDay: Int, adjustDays: Int = 1): JalaliDate? {
        val midYear = jalaliToGregorian(jy, 7, 1)
        val jdnMid = midYear.toEpochDay() + 2440588L - adjustDays
        val baseHy = ((30 * (jdnMid - 1948440) + 10646) / 10631).toInt()
        for (candidate in baseHy - 1..baseHy + 2) {
            val j = hijriToJalali(candidate, hijriMonth, hijriDay, adjustDays)
            if (j.year == jy) return j
        }
        return null
    }

    // ───────────────────────── Week & names ─────────────────────────

    /** 0 = Saturday … 6 = Friday (Persian week order). */
    fun persianWeekIndex(dow: DayOfWeek): Int = (dow.value + 1) % 7

    fun persianWeekIndex(epochDay: Long): Int = persianWeekIndex(LocalDate.ofEpochDay(epochDay).dayOfWeek)

    private val FA_WEEKDAYS = arrayOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    private val FA_JALALI_MONTHS = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private val EN_JALALI_MONTHS = arrayOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )
    val HIJRI_MONTHS_FA = arrayOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )

    fun weekdayName(epochDay: Long, persianLocale: Boolean): String {
        val idx = persianWeekIndex(epochDay)
        return if (persianLocale) FA_WEEKDAYS[idx]
        else LocalDate.ofEpochDay(epochDay).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    fun jalaliMonthName(month: Int, persianLocale: Boolean): String =
        (if (persianLocale) FA_JALALI_MONTHS else EN_JALALI_MONTHS)[month - 1]

    fun jalaliMonthName(j: JalaliDate, persianLocale: Boolean): String = jalaliMonthName(j.month, persianLocale)

    fun gregorianMonthName(month: Int, persianLocale: Boolean): String {
        val m = java.time.Month.of(month)
        return if (persianLocale) {
            arrayOf(
                "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
                "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
            )[month - 1]
        } else m.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    /** Full pretty date, e.g. «شنبه ۲۵ مهر ۱۴۰۵» or «Saturday 25 Mehr 1405». */
    fun formatFullDate(
        epochDay: Long,
        jalali: Boolean,
        persianLocale: Boolean,
        digits: (String) -> String = { it }
    ): String {
        val ld = LocalDate.ofEpochDay(epochDay)
        val weekday = weekdayName(epochDay, persianLocale)
        return if (jalali) {
            val j = gregorianToJalali(ld)
            digits("$weekday ${j.day} ${jalaliMonthName(j.month, persianLocale)} ${j.year}")
        } else {
            digits("$weekday ${ld.dayOfMonth} ${gregorianMonthName(ld.monthValue, persianLocale)} ${ld.year}")
        }
    }

    /** Short pretty date without weekday, e.g. «۲۵ مهر ۱۴۰۵». */
    fun formatShortDate(
        epochDay: Long,
        jalali: Boolean,
        persianLocale: Boolean,
        digits: (String) -> String = { it }
    ): String {
        val ld = LocalDate.ofEpochDay(epochDay)
        return if (jalali) {
            val j = gregorianToJalali(ld)
            digits("${j.day} ${jalaliMonthName(j.month, persianLocale)} ${j.year}")
        } else {
            digits("${ld.dayOfMonth} ${gregorianMonthName(ld.monthValue, persianLocale)} ${ld.year}")
        }
    }

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun floorMod(a: Long, b: Int): Int = ((a % b + b) % b).toInt()
}
