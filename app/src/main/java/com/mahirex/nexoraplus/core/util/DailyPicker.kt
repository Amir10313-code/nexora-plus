package com.mahirex.nexoraplus.core.util

/**
 * Deterministic daily picker: the same day + salt always yields the same item,
 * so the fortune shown on the Home header is exactly the one in the fortune page.
 */
object DailyPicker {

    fun indexOf(size: Int, epochDay: Long, salt: Int): Int {
        require(size > 0) { "size must be > 0" }
        var h = epochDay * 1_000_003L + salt * 31L
        h = h xor (h ushr 33)
        h *= 0xFF51AFD7ED558CCDL
        h = h xor (h ushr 33)
        h *= 0xC4CEB9FE1A85EC53L
        h = h xor (h ushr 33)
        // Clear the sign bit instead of abs(): abs(Long.MIN_VALUE) overflows and stays negative,
        // which would produce a negative index (IndexOutOfBoundsException) in pick().
        return ((h and Long.MAX_VALUE) % size).toInt()
    }

    fun <T> pick(list: List<T>, epochDay: Long, salt: Int): T = list[indexOf(list.size, epochDay, salt)]
}
