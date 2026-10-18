package com.mahirex.nexoraplus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// ─────────────────────────────── Events (calendar) ───────────────────────────────
@Serializable
@Entity(tableName = "events", indices = [Index("accountId"), Index("epochDay")])
data class CalEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val description: String = "",
    val epochDay: Long,
    val timeMinutes: Int? = null,
    val categoryId: Long? = null,
    val colorHex: String = "#5B3FE0",
    val isPinned: Boolean = false,
    val showCountdown: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────── Event categories ───────────────────────────────
@Serializable
@Entity(tableName = "event_categories", indices = [Index("accountId")])
data class EventCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val name: String,
    val emoji: String = "📌",
    val colorHex: String = "#5B3FE0",
    val isSystem: Boolean = false
)

// ─────────────────────────────── Weekly schedule ───────────────────────────────
/** A reusable time block (row) of the weekly table. Fully editable by the user. */
@Serializable
@Entity(tableName = "schedule_slots", indices = [Index("accountId"), Index("orderIndex")])
data class ScheduleSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val colorHex: String = "#5B3FE0",
    val orderIndex: Int
)

/** One colored cell: an activity planned inside a slot on a specific weekday (0=Sat..6=Fri). */
@Serializable
@Entity(tableName = "schedule_entries", indices = [Index("accountId"), Index("slotId")])
data class ScheduleEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val dayIndex: Int,
    val slotId: Long,
    val title: String,
    val note: String = "",
    val colorHex: String = "#7C5CFF"
)

/** A saved layout of time blocks — “ذخیره به‌عنوان پیش‌فرض”. */
@Serializable
@Entity(tableName = "schedule_presets", indices = [Index("accountId")])
data class SchedulePresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val name: String,
    val slotsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────── Fortune history ───────────────────────────────
@Serializable
@Entity(tableName = "fortune_history", indices = [Index("accountId"), Index(value = ["accountId", "epochDay"], unique = true)])
data class FortuneHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val epochDay: Long,
    val state: Int,                     // 0 excellent / 1 good / 2 medium / 3 caution
    val verse: String,
    val tafseer: String,
    val quote: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────── Occasions (امروز در تقویم) ───────────────────────────────
/**
 * Shared across all accounts. calType: 0 = lunar (Hijri), 1 = solar (Jalali).
 * kind: 0 = religious, 1 = national, 2 = international/cultural.
 */
@Serializable
@Entity(
    tableName = "occasions",
    indices = [Index("calType"), Index(value = ["calType", "month", "day"])]
)
data class OccasionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val calType: Int,
    val month: Int,
    val day: Int,
    val kind: Int,
    val title: String,
    val description: String,
    val isHoliday: Boolean,
    val emoji: String = "🕌",
    val colorHex: String = "#5B3FE0",
    val version: Int = 1
)

// ─────────────────────────────── Focus sessions ───────────────────────────────
@Serializable
@Entity(tableName = "focus_sessions", indices = [Index("accountId")])
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val epochDay: Long,
    val minutes: Int,
    val label: String = "",
    val startedAt: Long = System.currentTimeMillis()
)
