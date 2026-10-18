package com.mahirex.nexoraplus.data.fortune

import com.mahirex.nexoraplus.core.util.DailyPicker

data class DailyFortune(
    val epochDay: Long,
    val state: Int,          // 0 excellent / 1 good / 2 medium / 3 caution
    val verse: String,
    val tafseer: String,
    val quote: String,
    val tip: String,
    val guidance: List<String> // size 5: love, work, health, money, energy
)

/**
 * Deterministic fortune engine: for a given day + account the exact same
 * fortune is produced everywhere (home header ↔ fortune page ↔ widget).
 */
object FortuneEngine {

    const val DOMAIN_COUNT = 5

    private fun stateOf(epochDay: Long, salt: Long): Int {
        val roll = DailyPicker.indexOf(100, epochDay, salt.toInt() + 77)
        return when {
            roll < 26 -> 0
            roll < 66 -> 1
            roll < 88 -> 2
            else -> 3
        }
    }

    fun build(epochDay: Long, salt: Long): DailyFortune {
        val state = stateOf(epochDay, salt)
        val hafez = DailyPicker.pick(HafezData.entries, epochDay, salt.toInt())
        val quote = DailyPicker.pick(QuotesData.quotes, epochDay, salt.toInt() + 11)
        val tip = DailyPicker.pick(TipsData.tips, epochDay, salt.toInt() + 23)
        val guidance = (0 until DOMAIN_COUNT).map { domain ->
            val pool = GuidanceData.lines[domain][state]
            DailyPicker.pick(pool, epochDay, salt.toInt() + 40 * (domain + 1))
        }
        return DailyFortune(
            epochDay = epochDay,
            state = state,
            verse = hafez.verse,
            tafseer = hafez.tafseer,
            quote = quote,
            tip = tip,
            guidance = guidance
        )
    }
}
