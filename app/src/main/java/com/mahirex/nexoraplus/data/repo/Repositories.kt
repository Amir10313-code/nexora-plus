package com.mahirex.nexoraplus.data.repo

import android.content.Context
import androidx.room.withTransaction
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.occasions.DefaultOccasions
import com.mahirex.nexoraplus.data.occasions.OccasionSyncManager
import com.mahirex.nexoraplus.data.fortune.DailyFortune
import com.mahirex.nexoraplus.data.local.NexoraDatabase
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.data.local.entity.ChecklistItemEntity
import com.mahirex.nexoraplus.data.local.entity.EventCategoryEntity
import com.mahirex.nexoraplus.data.local.entity.FortuneHistoryEntity
import com.mahirex.nexoraplus.data.local.entity.GoalEntity
import com.mahirex.nexoraplus.data.local.entity.HabitEntity
import com.mahirex.nexoraplus.data.local.entity.HabitLogEntity
import com.mahirex.nexoraplus.data.local.entity.ListItemEntity
import com.mahirex.nexoraplus.data.local.entity.NoteEntity
import com.mahirex.nexoraplus.data.local.entity.OccasionEntity
import com.mahirex.nexoraplus.data.local.entity.SchedulePresetEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleSlotEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────── Accounts ───────────────────────────────
class AccountRepository(private val dao: NexoraDatabase) {
    val accounts: Flow<List<AccountEntity>> = dao.accountDao().observeAll()
    suspend fun byId(id: Long): AccountEntity? = dao.accountDao().byId(id)
    suspend fun count(): Int = dao.accountDao().count()
    suspend fun create(name: String, avatarKey: String, gender: String, colorHex: String): Long =
        dao.accountDao().insert(AccountEntity(name = name, avatarKey = avatarKey, gender = gender, colorHex = colorHex))

    suspend fun update(account: AccountEntity) = dao.accountDao().update(account)
    suspend fun delete(id: Long) = dao.accountDao().deleteById(id)
    suspend fun touch(id: Long) = dao.accountDao().touch(id, System.currentTimeMillis())
}

// ─────────────────────────────── Tasks ───────────────────────────────
class TaskRepository(private val dao: NexoraDatabase) {
    fun tasks(accountId: Long, done: Boolean?): Flow<List<TaskEntity>> =
        if (done == null) dao.taskDao().observeAll(accountId) else dao.taskDao().observeByDone(accountId, done)

    fun forDay(accountId: Long, epochDay: Long): Flow<List<TaskEntity>> = dao.taskDao().observeForDay(accountId, epochDay)
    fun search(accountId: Long, q: String): Flow<List<TaskEntity>> = dao.taskDao().search(accountId, q)

    suspend fun save(task: TaskEntity): Long = dao.taskDao().upsert(task)
    suspend fun setDone(id: Long, done: Boolean) =
        dao.taskDao().setDone(id, done, if (done) System.currentTimeMillis() else null)

    suspend fun setPinned(id: Long, pinned: Boolean) = dao.taskDao().setPinned(id, pinned)
    suspend fun delete(id: Long) = dao.taskDao().deleteById(id)
    suspend fun deleteAllFor(accountId: Long) = dao.taskDao().deleteAllForAccount(accountId)
    suspend fun counts(accountId: Long): Pair<Int, Int> =
        dao.taskDao().countForAccount(accountId) to dao.taskDao().doneCount(accountId)
}

// ─────────────────────────────── Goals ───────────────────────────────
class GoalRepository(private val dao: NexoraDatabase) {
    fun goals(accountId: Long): Flow<List<GoalEntity>> = dao.goalDao().observeAll(accountId)
    fun search(accountId: Long, q: String): Flow<List<GoalEntity>> = dao.goalDao().search(accountId, q)
    suspend fun save(goal: GoalEntity): Long = dao.goalDao().upsert(goal)
    suspend fun setProgress(id: Long, p: Int) = dao.goalDao().setProgress(id, p)
    suspend fun setPinned(id: Long, p: Boolean) = dao.goalDao().setPinned(id, p)
    suspend fun delete(id: Long) = dao.goalDao().deleteById(id)
    suspend fun deleteAllFor(accountId: Long) = dao.goalDao().deleteAllForAccount(accountId)
}

// ─────────────────────────────── Habits ───────────────────────────────
class HabitRepository(private val dao: NexoraDatabase) {
    fun habits(accountId: Long): Flow<List<HabitEntity>> = dao.habitDao().observeAll(accountId)
    fun logsSince(accountId: Long, fromEpochDay: Long): Flow<List<HabitLogEntity>> =
        dao.habitDao().observeLogsSince(accountId, fromEpochDay)
    fun search(accountId: Long, q: String): Flow<List<HabitEntity>> = dao.habitDao().search(accountId, q)

    suspend fun save(habit: HabitEntity): Long = dao.habitDao().upsert(habit)

    suspend fun delete(habit: HabitEntity) = dao.withTransaction {
        dao.habitDao().deleteLogsOfHabit(habit.id)
        dao.habitDao().deleteById(habit.id)
    }

    suspend fun deleteAllFor(accountId: Long) = dao.withTransaction {
        dao.habitDao().deleteAllLogsForAccount(accountId)
        dao.habitDao().deleteAllForAccount(accountId)
    }

    suspend fun toggle(habitId: Long, accountId: Long, epochDay: Long) {
        val existing = dao.habitDao().logFor(habitId, epochDay)
        if (existing != null) dao.habitDao().deleteLog(habitId, epochDay)
        else dao.habitDao().insertLog(HabitLogEntity(habitId = habitId, accountId = accountId, epochDay = epochDay))
    }

    /** Consecutive done-days ending today (or yesterday if today not yet done). */
    fun streak(logs: List<HabitLogEntity>, habitId: Long, today: Long): Int {
        val days = logs.filter { it.habitId == habitId }.map { it.epochDay }.toHashSet()
        var cursor = today
        if (cursor !in days) cursor -= 1
        var streak = 0
        while (cursor in days) {
            streak++
            cursor -= 1
        }
        return streak
    }
}

// ─────────────────────────────── Notes ───────────────────────────────
class NoteRepository(private val dao: NexoraDatabase) {
    fun notes(accountId: Long): Flow<List<NoteEntity>> = dao.noteDao().observeAll(accountId)
    fun search(accountId: Long, q: String): Flow<List<NoteEntity>> = dao.noteDao().search(accountId, q)
    suspend fun save(note: NoteEntity): Long = dao.noteDao().upsert(note)
    suspend fun delete(id: Long) = dao.noteDao().deleteById(id)
    suspend fun deleteAllFor(accountId: Long) = dao.noteDao().deleteAllForAccount(accountId)
}

// ─────────────────────────────── Lists ───────────────────────────────
class ListsRepository(private val dao: NexoraDatabase) {
    fun lists(accountId: Long): Flow<List<ListItemEntity>> = dao.listsDao().observeAll(accountId)
    fun items(listId: Long): Flow<List<ChecklistItemEntity>> = dao.listsDao().observeItems(listId)
    suspend fun save(list: ListItemEntity): Long = dao.listsDao().upsert(list)
    suspend fun saveItem(item: ChecklistItemEntity): Long = dao.listsDao().upsertItem(item)
    suspend fun setItemDone(id: Long, done: Boolean) = dao.listsDao().setItemDone(id, done)
    suspend fun deleteList(id: Long) = dao.withTransaction {
        dao.listsDao().deleteItemsOfList(id)
        dao.listsDao().deleteById(id)
    }
    suspend fun deleteItem(id: Long) = dao.listsDao().deleteItem(id)
    suspend fun deleteAllFor(accountId: Long) = dao.withTransaction {
        dao.listsDao().deleteAllItemsForAccount(accountId)
        dao.listsDao().deleteAllForAccount(accountId)
    }
}

// ─────────────────────────────── Calendar & occasions ───────────────────────────────
class CalendarRepository(private val dao: NexoraDatabase) {
    fun eventsBetween(accountId: Long, from: Long, to: Long): Flow<List<CalEventEntity>> =
        dao.calendarDao().observeBetween(accountId, from, to)

    fun eventsForDay(accountId: Long, epochDay: Long): Flow<List<CalEventEntity>> =
        dao.calendarDao().observeForDay(accountId, epochDay)

    fun nextCountdown(accountId: Long, today: Long): Flow<CalEventEntity?> =
        dao.calendarDao().observeNextCountdown(accountId, today)

    fun search(accountId: Long, q: String): Flow<List<CalEventEntity>> = dao.calendarDao().search(accountId, q)

    /** Live event count — Room re-emits whenever the events table changes. */
    fun count(accountId: Long): Flow<Int> = dao.calendarDao().observeCountForAccount(accountId)

    suspend fun save(event: CalEventEntity): Long = dao.calendarDao().upsert(event)
    suspend fun setPinned(id: Long, p: Boolean) = dao.calendarDao().setPinned(id, p)
    suspend fun delete(id: Long) = dao.calendarDao().deleteById(id)
    suspend fun deleteAllFor(accountId: Long) = dao.calendarDao().deleteAllForAccount(accountId)

    fun categories(accountId: Long): Flow<List<EventCategoryEntity>> = dao.calendarDao().observeCategories(accountId)
    suspend fun saveCategory(c: EventCategoryEntity): Long = dao.calendarDao().upsertCategory(c)
    suspend fun deleteCategory(id: Long) = dao.withTransaction {
        dao.calendarDao().detachEventsOfCategory(id)
        dao.calendarDao().deleteCategory(id)
    }

    /**
     * Full occasion list for a Jalali date: solar (Jalali) occasions by month/day.
     * Iranian religious/national occasions are defined by the Jalali & Hijri calendars,
     * so the lookup always resolves through the real Jalali date of the day.
     * Note: lunar occasions must be merged separately via [lunarOccasionsFor]
     * (they move every year, so they cannot be matched by a static month/day query).
     */
    fun occasionsForJalaliDay(jalali: com.mahirex.nexoraplus.core.calendar.JalaliDate): Flow<List<OccasionEntity>> {
        return dao.occasionDao().observeForDate(calType = 1, month = jalali.month, day = jalali.day)
    }

    suspend fun lunarOccasionsFor(jalali: com.mahirex.nexoraplus.core.calendar.JalaliDate): List<OccasionEntity> {
        val all = dao.occasionDao().all()
        return all.filter { it.calType == 0 }.filter {
            val j = CalendarConverter.hijriOccasionInJalaliYear(jalali.year, it.month, it.day)
            j != null && j.month == jalali.month && j.day == jalali.day
        }
    }

    suspend fun count(): Int = dao.occasionDao().count()
    suspend fun maxVersion(): Int = dao.occasionDao().maxVersion() ?: 1
}

// ─────────────────────────────── Weekly schedule ───────────────────────────────
class ScheduleRepository(private val dao: NexoraDatabase) {
    fun slots(accountId: Long): Flow<List<ScheduleSlotEntity>> = dao.scheduleDao().observeSlots(accountId)
    fun entries(accountId: Long): Flow<List<com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity>> =
        dao.scheduleDao().observeEntries(accountId)

    fun presets(accountId: Long): Flow<List<SchedulePresetEntity>> = dao.scheduleDao().observePresets(accountId)

    suspend fun saveSlot(slot: ScheduleSlotEntity): Long = dao.scheduleDao().upsertSlot(slot)
    suspend fun deleteSlot(slot: ScheduleSlotEntity) = dao.withTransaction {
        dao.scheduleDao().deleteEntriesOfSlot(slot.id)
        dao.scheduleDao().deleteSlot(slot.id)
    }

    suspend fun saveEntry(entry: com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity): Long =
        dao.scheduleDao().upsertEntry(entry)

    suspend fun deleteEntry(id: Long) = dao.scheduleDao().deleteEntry(id)

    suspend fun applySlots(accountId: Long, slots: List<ScheduleSlotEntity>) = dao.withTransaction {
        dao.scheduleDao().deleteAllEntries(accountId)
        dao.scheduleDao().deleteAllSlots(accountId)
        slots.forEachIndexed { i, s ->
            dao.scheduleDao().upsertSlot(s.copy(id = 0, accountId = accountId, orderIndex = i))
        }
    }

    suspend fun savePreset(preset: SchedulePresetEntity): Long = dao.scheduleDao().upsertPreset(preset)
    suspend fun deletePreset(id: Long) = dao.scheduleDao().deletePreset(id)
    suspend fun currentSlots(accountId: Long): List<ScheduleSlotEntity> = dao.scheduleDao().slotsOf(accountId)
}

/** Factory default weekly slots (fully editable & deletable by the user afterwards). */
object ScheduleDefaults {
    fun of(context: Context, accountId: Long): List<ScheduleSlotEntity> = listOf(
        ScheduleSlotEntity(accountId = accountId, title = context.getString(R.string.slot_morning), startMinutes = 6 * 60, endMinutes = 9 * 60, colorHex = "#F59E0B", orderIndex = 0),
        ScheduleSlotEntity(accountId = accountId, title = context.getString(R.string.slot_work), startMinutes = 9 * 60, endMinutes = 13 * 60, colorHex = "#5B3FE0", orderIndex = 1),
        ScheduleSlotEntity(accountId = accountId, title = context.getString(R.string.slot_rest), startMinutes = 13 * 60, endMinutes = 15 * 60, colorHex = "#0891B2", orderIndex = 2),
        ScheduleSlotEntity(accountId = accountId, title = context.getString(R.string.slot_sport), startMinutes = 17 * 60, endMinutes = 19 * 60, colorHex = "#059669", orderIndex = 3),
        ScheduleSlotEntity(accountId = accountId, title = context.getString(R.string.slot_evening), startMinutes = 19 * 60, endMinutes = 22 * 60, colorHex = "#DB2777", orderIndex = 4),
        ScheduleSlotEntity(accountId = accountId, title = context.getString(R.string.slot_night), startMinutes = 22 * 60, endMinutes = 23 * 60 + 30, colorHex = "#4F46E5", orderIndex = 5)
    )
}

// ─────────────────────────────── Fortune ───────────────────────────────
class FortuneRepository(private val dao: NexoraDatabase) {
    fun history(accountId: Long): Flow<List<FortuneHistoryEntity>> = dao.fortuneDao().observeHistory(accountId)
    suspend fun record(accountId: Long, fortune: DailyFortune) {
        val existing = dao.fortuneDao().forDay(accountId, fortune.epochDay)
        if (existing == null) {
            dao.fortuneDao().upsert(
                FortuneHistoryEntity(
                    accountId = accountId,
                    epochDay = fortune.epochDay,
                    state = fortune.state,
                    verse = fortune.verse,
                    tafseer = fortune.tafseer,
                    quote = fortune.quote
                )
            )
        }
    }
    suspend fun deleteAllFor(accountId: Long) = dao.fortuneDao().deleteAllForAccount(accountId)
}

// ─────────────────────────────── Focus ───────────────────────────────
class FocusRepository(private val dao: NexoraDatabase) {
    fun sessionsToday(accountId: Long, epochDay: Long): Flow<List<com.mahirex.nexoraplus.data.local.entity.FocusSessionEntity>> =
        dao.focusDao().observeForDay(accountId, epochDay)

    fun minutesToday(accountId: Long, epochDay: Long): Flow<Int> = dao.focusDao().observeMinutesForDay(accountId, epochDay)
    /** Live focus total — Room re-emits whenever focus_sessions changes. */
    fun total(accountId: Long): Flow<Int> = dao.focusDao().observeTotalMinutes(accountId)
    suspend fun add(accountId: Long, epochDay: Long, minutes: Int, label: String) =
        dao.focusDao().insert(
            com.mahirex.nexoraplus.data.local.entity.FocusSessionEntity(
                accountId = accountId, epochDay = epochDay, minutes = minutes, label = label
            )
        )
    suspend fun deleteAllFor(accountId: Long) = dao.focusDao().deleteAllForAccount(accountId)
}

// ─────────────────────────────── Occasions management ───────────────────────────────
class OccasionAdminRepository(private val dao: NexoraDatabase) {
    suspend fun count(): Int = dao.occasionDao().count()
    suspend fun seedBundled() {
        if (dao.occasionDao().count() == 0) {
            dao.occasionDao().insertAll(DefaultOccasions.all)
        }
    }

    suspend fun replaceWith(remote: OccasionSyncManager.OccasionFile) {
        val entities = remote.occasions.map {
            OccasionEntity(
                calType = it.calType, month = it.month, day = it.day, kind = it.kind,
                title = it.title, description = it.description, isHoliday = it.isHoliday,
                emoji = it.emoji, colorHex = it.colorHex, version = remote.version
            )
        }
        // Atomic swap: without a transaction the home/calendar screens observe an
        // empty occasion list mid-update, and a crash in between leaves the table wiped.
        dao.withTransaction {
            dao.occasionDao().clear()
            dao.occasionDao().insertAll(entities)
        }
    }
}
