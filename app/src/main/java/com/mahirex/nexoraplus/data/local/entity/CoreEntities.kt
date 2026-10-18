package com.mahirex.nexoraplus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// ─────────────────────────────── Account ───────────────────────────────
@Serializable
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarKey: String = "🌟",
    val gender: String = "u",           // m / f / u
    val colorHex: String = "#5B3FE0",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────── Tasks ───────────────────────────────
@Serializable
@Entity(tableName = "tasks", indices = [Index("accountId")])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val description: String = "",
    val dueEpochDay: Long? = null,
    val timeMinutes: Int? = null,
    val priority: Int = 0,              // 0 low / 1 medium / 2 high
    val colorHex: String = "#5B3FE0",
    val isPinned: Boolean = false,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val doneAt: Long? = null
)

// ─────────────────────────────── Goals ───────────────────────────────
@Serializable
@Entity(tableName = "goals", indices = [Index("accountId")])
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val description: String = "",
    val targetEpochDay: Long? = null,
    val progress: Int = 0,              // 0..100
    val colorHex: String = "#059669",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────── Habits ───────────────────────────────
@Serializable
@Entity(tableName = "habits", indices = [Index("accountId")])
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val colorHex: String = "#0891B2",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "epochDay"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val accountId: Long,
    val epochDay: Long
)

// ─────────────────────────────── Notes ───────────────────────────────
@Serializable
@Entity(tableName = "notes", indices = [Index("accountId")])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val content: String = "",
    val colorHex: String = "#DB2777",
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────── Lists (checklists) ───────────────────────────────
@Serializable
@Entity(tableName = "lists", indices = [Index("accountId")])
data class ListItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val title: String,
    val emoji: String = "📝",
    val colorHex: String = "#0891B2",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = "list_items",
    indices = [Index("listId"), Index(value = ["listId", "orderIndex"])]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val accountId: Long,
    val title: String,
    val isDone: Boolean = false,
    val orderIndex: Int = 0
)
