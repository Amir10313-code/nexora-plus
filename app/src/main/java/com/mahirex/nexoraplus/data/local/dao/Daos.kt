package com.mahirex.nexoraplus.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.data.local.entity.ChecklistItemEntity
import com.mahirex.nexoraplus.data.local.entity.EventCategoryEntity
import com.mahirex.nexoraplus.data.local.entity.FocusSessionEntity
import com.mahirex.nexoraplus.data.local.entity.FortuneHistoryEntity
import com.mahirex.nexoraplus.data.local.entity.GoalEntity
import com.mahirex.nexoraplus.data.local.entity.HabitEntity
import com.mahirex.nexoraplus.data.local.entity.HabitLogEntity
import com.mahirex.nexoraplus.data.local.entity.ListItemEntity
import com.mahirex.nexoraplus.data.local.entity.NoteEntity
import com.mahirex.nexoraplus.data.local.entity.OccasionEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity
import com.mahirex.nexoraplus.data.local.entity.SchedulePresetEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleSlotEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────── Accounts ───────────────────────────────
@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY lastUsedAt DESC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun byId(id: Long): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE accounts SET lastUsedAt = :ts WHERE id = :id")
    suspend fun touch(id: Long, ts: Long)

    @Query("SELECT * FROM accounts")
    suspend fun observeAllSnapshot(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun deleteEverything()
}

// ─────────────────────────────── Tasks ───────────────────────────────
@Dao
interface TaskDao {
    @Query(
        "SELECT * FROM tasks WHERE accountId = :a AND isDone = :done ORDER BY isPinned DESC, " +
            "dueEpochDay IS NULL, dueEpochDay ASC, priority DESC, createdAt DESC"
    )
    fun observeByDone(a: Long, done: Boolean): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE accountId = :a ORDER BY isPinned DESC, createdAt DESC")
    fun observeAll(a: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE accountId = :a AND dueEpochDay = :day AND isDone = 0 ORDER BY isPinned DESC, priority DESC")
    fun observeForDay(a: Long, day: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun byId(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET isDone = :done, doneAt = :doneAt WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, doneAt: Long?)

    @Query("UPDATE tasks SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT COUNT(*) FROM tasks WHERE accountId = :a")
    suspend fun countForAccount(a: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE accountId = :a AND isDone = 1")
    suspend fun doneCount(a: Long): Int

    @Query("SELECT * FROM tasks WHERE accountId = :a AND title LIKE '%' || :q || '%' ORDER BY isPinned DESC LIMIT 20")
    fun search(a: Long, q: String): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks")
    suspend fun deleteEverything()
}

// ─────────────────────────────── Goals ───────────────────────────────
@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE accountId = :a ORDER BY isPinned DESC, createdAt DESC")
    fun observeAll(a: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun byId(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity): Long

    @Query("UPDATE goals SET progress = :progress WHERE id = :id")
    suspend fun setProgress(id: Long, progress: Int)

    @Query("UPDATE goals SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM goals WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT COUNT(*) FROM goals WHERE accountId = :a")
    suspend fun countForAccount(a: Long): Int

    @Query("SELECT * FROM goals WHERE accountId = :a AND title LIKE '%' || :q || '%' LIMIT 20")
    fun search(a: Long, q: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(goals: List<GoalEntity>)

    @Query("DELETE FROM goals")
    suspend fun deleteEverything()
}

// ─────────────────────────────── Habits ───────────────────────────────
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE accountId = :a ORDER BY createdAt ASC")
    fun observeAll(a: Long): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun byId(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity): Long

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM habits WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT * FROM habit_logs WHERE accountId = :a AND epochDay >= :from")
    fun observeLogsSince(a: Long, from: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND epochDay = :day")
    suspend fun logFor(habitId: Long, day: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND epochDay = :day")
    suspend fun deleteLog(habitId: Long, day: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsOfHabit(habitId: Long)

    @Query("DELETE FROM habit_logs WHERE accountId = :a")
    suspend fun deleteAllLogsForAccount(a: Long)

    @Query("SELECT * FROM habits WHERE accountId = :a AND title LIKE '%' || :q || '%' LIMIT 20")
    fun search(a: Long, q: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(habit: HabitEntity)

    @Query("SELECT * FROM habit_logs WHERE accountId = :a")
    suspend fun logsFor(a: Long): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogsKeep(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteLogsEverything()

    @Query("DELETE FROM habits")
    suspend fun deleteEverything()
}

// ─────────────────────────────── Notes ───────────────────────────────
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE accountId = :a ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAll(a: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun byId(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT * FROM notes WHERE accountId = :a AND (title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%') LIMIT 20")
    fun search(a: Long, q: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(notes: List<NoteEntity>)

    @Query("DELETE FROM notes")
    suspend fun deleteEverything()
}

// ─────────────────────────────── Lists ───────────────────────────────
@Dao
interface ListsDao {
    @Query("SELECT * FROM lists WHERE accountId = :a ORDER BY createdAt DESC")
    fun observeAll(a: Long): Flow<List<ListItemEntity>>

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun byId(id: Long): ListItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: ListItemEntity): Long

    @Query("DELETE FROM lists WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM lists WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT * FROM list_items WHERE listId = :listId ORDER BY orderIndex ASC, id ASC")
    fun observeItems(listId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM list_items WHERE listId = :listId ORDER BY orderIndex ASC, id ASC")
    suspend fun itemsOf(listId: Long): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ChecklistItemEntity): Long

    @Query("UPDATE list_items SET isDone = :done WHERE id = :id")
    suspend fun setItemDone(id: Long, done: Boolean)

    @Query("DELETE FROM list_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM list_items WHERE listId = :listId")
    suspend fun deleteItemsOfList(listId: Long)

    @Query("DELETE FROM list_items WHERE accountId = :a")
    suspend fun deleteAllItemsForAccount(a: Long)

    @Query("SELECT COUNT(*) FROM lists WHERE accountId = :a")
    suspend fun countForAccount(a: Long): Int

    @Query("SELECT * FROM lists WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<ListItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(list: ListItemEntity)

    @Query("SELECT * FROM list_items WHERE accountId = :a")
    suspend fun itemsFor(a: Long): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemsKeep(items: List<ChecklistItemEntity>)

    @Query("DELETE FROM list_items")
    suspend fun deleteItemsEverything()

    @Query("DELETE FROM lists")
    suspend fun deleteEverything()
}

// ─────────────────────────────── Events & categories ───────────────────────────────
@Dao
interface CalendarDao {
    @Query("SELECT * FROM events WHERE accountId = :a AND epochDay BETWEEN :from AND :to ORDER BY isPinned DESC, timeMinutes IS NULL, timeMinutes ASC")
    fun observeBetween(a: Long, from: Long, to: Long): Flow<List<CalEventEntity>>

    @Query("SELECT * FROM events WHERE accountId = :a AND epochDay = :day ORDER BY isPinned DESC, timeMinutes IS NULL, timeMinutes ASC")
    fun observeForDay(a: Long, day: Long): Flow<List<CalEventEntity>>

    @Query("SELECT * FROM events WHERE accountId = :a AND showCountdown = 1 AND epochDay >= :today ORDER BY epochDay ASC LIMIT 1")
    fun observeNextCountdown(a: Long, today: Long): Flow<CalEventEntity?>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun byId(id: Long): CalEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: CalEventEntity): Long

    @Query("UPDATE events SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM events WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT COUNT(*) FROM events WHERE accountId = :a")
    suspend fun countForAccount(a: Long): Int

    /** Live count — recomputed by Room whenever events change. */
    @Query("SELECT COUNT(*) FROM events WHERE accountId = :a")
    fun observeCountForAccount(a: Long): Flow<Int>

    @Query("SELECT * FROM events WHERE accountId = :a AND title LIKE '%' || :q || '%' ORDER BY epochDay ASC LIMIT 20")
    fun search(a: Long, q: String): Flow<List<CalEventEntity>>

    @Query("SELECT * FROM event_categories WHERE accountId = :a ORDER BY isSystem DESC, name ASC")
    fun observeCategories(a: Long): Flow<List<EventCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: EventCategoryEntity): Long

    @Query("DELETE FROM event_categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Query("UPDATE events SET categoryId = NULL WHERE categoryId = :id")
    suspend fun detachEventsOfCategory(id: Long)

    @Query("SELECT COUNT(*) FROM event_categories WHERE accountId = :a")
    suspend fun categoryCount(a: Long): Int

    @Query("SELECT * FROM events WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<CalEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(events: List<CalEventEntity>)

    @Query("DELETE FROM events")
    suspend fun deleteEverything()

    @Query("SELECT * FROM event_categories WHERE accountId = :a")
    suspend fun categoriesFor(a: Long): List<EventCategoryEntity>

    @Query("DELETE FROM event_categories WHERE accountId = :a")
    suspend fun deleteCategoriesForAccount(a: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryKeep(category: EventCategoryEntity)

    @Query("DELETE FROM event_categories")
    suspend fun deleteCategoriesEverything()
}

// ─────────────────────────────── Weekly schedule ───────────────────────────────
@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_slots WHERE accountId = :a ORDER BY orderIndex ASC, startMinutes ASC")
    fun observeSlots(a: Long): Flow<List<ScheduleSlotEntity>>

    @Query("SELECT * FROM schedule_slots WHERE accountId = :a ORDER BY orderIndex ASC, startMinutes ASC")
    suspend fun slotsOf(a: Long): List<ScheduleSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: ScheduleSlotEntity): Long

    @Query("DELETE FROM schedule_slots WHERE id = :id")
    suspend fun deleteSlot(id: Long)

    @Query("DELETE FROM schedule_slots WHERE accountId = :a")
    suspend fun deleteAllSlots(a: Long)

    @Query("SELECT COUNT(*) FROM schedule_slots WHERE accountId = :a")
    suspend fun slotCount(a: Long): Int

    @Query("SELECT * FROM schedule_entries WHERE accountId = :a")
    fun observeEntries(a: Long): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE accountId = :a AND dayIndex = :day AND slotId = :slotId")
    suspend fun entriesFor(a: Long, day: Int, slotId: Long): List<ScheduleEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: ScheduleEntryEntity): Long

    @Query("DELETE FROM schedule_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("DELETE FROM schedule_entries WHERE slotId = :slotId")
    suspend fun deleteEntriesOfSlot(slotId: Long)

    @Query("DELETE FROM schedule_entries WHERE accountId = :a")
    suspend fun deleteAllEntries(a: Long)

    @Query("SELECT * FROM schedule_entries WHERE accountId = :a")
    suspend fun entriesOf(a: Long): List<ScheduleEntryEntity>

    @Query("SELECT * FROM schedule_presets WHERE accountId = :a")
    suspend fun presetsOf(a: Long): List<SchedulePresetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlotKeep(slot: ScheduleSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntryKeep(entry: ScheduleEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetKeep(preset: SchedulePresetEntity)

    @Query("DELETE FROM schedule_entries")
    suspend fun deleteAllEntriesEverything()

    @Query("DELETE FROM schedule_slots")
    suspend fun deleteAllSlotsEverything()

    @Query("DELETE FROM schedule_presets")
    suspend fun deletePresetsEverything()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreset(preset: SchedulePresetEntity): Long

    @Query("SELECT * FROM schedule_presets WHERE accountId = :a ORDER BY createdAt DESC")
    fun observePresets(a: Long): Flow<List<SchedulePresetEntity>>

    @Query("SELECT * FROM schedule_presets WHERE id = :id")
    suspend fun presetById(id: Long): SchedulePresetEntity?

    @Query("DELETE FROM schedule_presets WHERE id = :id")
    suspend fun deletePreset(id: Long)
}

// ─────────────────────────────── Fortune & focus & occasions ───────────────────────────────
@Dao
interface FortuneDao {
    @Query("SELECT * FROM fortune_history WHERE accountId = :a ORDER BY epochDay DESC")
    fun observeHistory(a: Long): Flow<List<FortuneHistoryEntity>>

    @Query("SELECT * FROM fortune_history WHERE accountId = :a AND epochDay = :day")
    suspend fun forDay(a: Long, day: Long): FortuneHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: FortuneHistoryEntity): Long

    @Query("DELETE FROM fortune_history WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT * FROM fortune_history WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<FortuneHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(history: List<FortuneHistoryEntity>)

    @Query("DELETE FROM fortune_history")
    suspend fun deleteEverything()
}

@Dao
interface FocusDao {
    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE accountId = :a AND epochDay = :day")
    fun observeForDay(a: Long, day: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM focus_sessions WHERE accountId = :a AND epochDay = :day")
    fun observeMinutesForDay(a: Long, day: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM focus_sessions WHERE accountId = :a")
    suspend fun totalMinutes(a: Long): Int

    /** Live total — recomputed by Room whenever focus_sessions changes. */
    @Query("SELECT COALESCE(SUM(minutes), 0) FROM focus_sessions WHERE accountId = :a")
    fun observeTotalMinutes(a: Long): Flow<Int>

    @Query("DELETE FROM focus_sessions WHERE accountId = :a")
    suspend fun deleteAllForAccount(a: Long)

    @Query("SELECT * FROM focus_sessions WHERE accountId = :a")
    suspend fun snapshotFor(a: Long): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeep(sessions: List<FocusSessionEntity>)

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteEverything()
}

@Dao
interface OccasionDao {
    @Query("SELECT * FROM occasions WHERE calType = :calType AND month = :month AND day = :day ORDER BY isHoliday DESC, kind ASC")
    fun observeForDate(calType: Int, month: Int, day: Int): Flow<List<OccasionEntity>>

    @Query("SELECT * FROM occasions")
    suspend fun all(): List<OccasionEntity>

    @Query("SELECT COUNT(*) FROM occasions")
    suspend fun count(): Int

    @Query("SELECT MAX(version) FROM occasions")
    suspend fun maxVersion(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(occasions: List<OccasionEntity>)

    @Query("DELETE FROM occasions WHERE version > :keepVersion")
    suspend fun deleteAboveVersion(keepVersion: Int)

    @Query("DELETE FROM occasions")
    suspend fun clear()
}
