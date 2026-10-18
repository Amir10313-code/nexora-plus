package com.mahirex.nexoraplus.core.util

/** Persian digit helpers — keeps every number in the UI beautiful and consistent. */
object PersianDigits {
    private val FA = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toFa(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(if (c in '0'..'9') FA[c - '0'] else c)
        }
        return sb.toString()
    }

    fun toLatin(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(if (c in FA) ('0' + (c - FA[0])) else c)
        }
        return sb.toString()
    }

    fun num(n: Int, persian: Boolean): String = if (persian) toFa(n.toString()) else n.toString()

    fun num(n: Long, persian: Boolean): String = if (persian) toFa(n.toString()) else n.toString()
}
