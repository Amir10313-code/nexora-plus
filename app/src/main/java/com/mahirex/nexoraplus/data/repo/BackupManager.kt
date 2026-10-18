package com.mahirex.nexoraplus.data.repo

import androidx.room.withTransaction
import com.mahirex.nexoraplus.data.local.NexoraDatabase
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
import com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity
import com.mahirex.nexoraplus.data.local.entity.SchedulePresetEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleSlotEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupFile(
    val app: String = "NexoraPlus",
    val schema: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val scope: String = "account",                  // account | all
    val accounts: List<AccountEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val lists: List<ListItemEntity> = emptyList(),
    val listItems: List<ChecklistItemEntity> = emptyList(),
    val events: List<CalEventEntity> = emptyList(),
    val categories: List<EventCategoryEntity> = emptyList(),
    val slots: List<ScheduleSlotEntity> = emptyList(),
    val entries: List<ScheduleEntryEntity> = emptyList(),
    val presets: List<SchedulePresetEntity> = emptyList(),
    val fortune: List<FortuneHistoryEntity> = emptyList(),
    val focus: List<FocusSessionEntity> = emptyList()
)

/**
 * Safe backup/restore engine.
 * - Account scope: replace-all of ONE account (ids are remapped to avoid collisions).
 * - All scope: full replace of the whole database (ids preserved).
 */
class BackupManager(private val db: NexoraDatabase) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ───────────────────── Export ─────────────────────
    suspend fun exportAccount(accountId: Long): String {
        val account = db.accountDao().byId(accountId)
        val file = BackupFile(
            scope = "account",
            accounts = if (account != null) listOf(account) else emptyList(),
            tasks = allTasks(accountId),
            goals = allGoals(accountId),
            habits = allHabits(accountId),
            habitLogs = allHabitLogs(accountId),
            notes = allNotes(accountId),
            lists = allLists(accountId),
            listItems = allListItems(accountId),
            events = allEvents(accountId),
            categories = allCategories(accountId),
            slots = allSlots(accountId),
            entries = allEntries(accountId),
            presets = allPresets(accountId),
            fortune = allFortune(accountId),
            focus = allFocus(accountId)
        )
        return json.encodeToString(file)
    }

    suspend fun exportAll(): String {
        val accounts = db.accountDao().observeAllSnapshot()
        var tasks = listOf<TaskEntity>()
        var goals = listOf<GoalEntity>()
        var habits = listOf<HabitEntity>()
        var logs = listOf<HabitLogEntity>()
        var notes = listOf<NoteEntity>()
        var lists = listOf<ListItemEntity>()
        var items = listOf<ChecklistItemEntity>()
        var events = listOf<CalEventEntity>()
        var cats = listOf<EventCategoryEntity>()
        var slots = listOf<ScheduleSlotEntity>()
        var entries = listOf<ScheduleEntryEntity>()
        var presets = listOf<SchedulePresetEntity>()
        var fortune = listOf<FortuneHistoryEntity>()
        var focus = listOf<FocusSessionEntity>()
        for (a in accounts) {
            tasks += allTasks(a.id); goals += allGoals(a.id); habits += allHabits(a.id)
            logs += allHabitLogs(a.id); notes += allNotes(a.id); lists += allLists(a.id)
            items += allListItems(a.id); events += allEvents(a.id); cats += allCategories(a.id)
            slots += allSlots(a.id); entries += allEntries(a.id); presets += allPresets(a.id)
            fortune += allFortune(a.id); focus += allFocus(a.id)
        }
        return json.encodeToString(
            BackupFile(
                scope = "all",
                accounts = accounts,
                tasks = tasks, goals = goals, habits = habits, habitLogs = logs,
                notes = notes, lists = lists, listItems = items, events = events,
                categories = cats, slots = slots, entries = entries, presets = presets,
                fortune = fortune, focus = focus
            )
        )
    }

    private suspend fun allTasks(a: Long) = db.taskDao().snapshotFor(a)
    private suspend fun allGoals(a: Long) = db.goalDao().snapshotFor(a)
    private suspend fun allHabits(a: Long) = db.habitDao().snapshotFor(a)
    private suspend fun allHabitLogs(a: Long) = db.habitDao().logsFor(a)
    private suspend fun allNotes(a: Long) = db.noteDao().snapshotFor(a)
    private suspend fun allLists(a: Long) = db.listsDao().snapshotFor(a)
    private suspend fun allListItems(a: Long) = db.listsDao().itemsFor(a)
    private suspend fun allEvents(a: Long) = db.calendarDao().snapshotFor(a)
    private suspend fun allCategories(a: Long) = db.calendarDao().categoriesFor(a)
    private suspend fun allSlots(a: Long) = db.scheduleDao().slotsOf(a)
    private suspend fun allEntries(a: Long) = db.scheduleDao().entriesOf(a)
    private suspend fun allPresets(a: Long) = db.scheduleDao().presetsOf(a)
    private suspend fun allFortune(a: Long) = db.fortuneDao().snapshotFor(a)
    private suspend fun allFocus(a: Long) = db.focusDao().snapshotFor(a)

    // ───────────────────── Import ─────────────────────
    suspend fun import(text: String, targetAccountId: Long?): Boolean {
        val file = try {
            json.decodeFromString<BackupFile>(text)
        } catch (e: Exception) {
            return false
        }
        // Resolve the target up-front: a scope="account" backup with no resolvable target
        // must report FAILURE, not fall through the transaction and return a silent no-op success.
        val resolvedTarget: Long = if (file.scope == "all") 0L else
            targetAccountId ?: file.accounts.firstOrNull()?.id ?: return false
        db.withTransaction {
            if (file.scope == "all") {
                wipeEverything()
                file.accounts.forEach { db.accountDao().insertKeep(it) }
                db.taskDao().insertKeep(file.tasks)
                db.goalDao().insertKeep(file.goals)
                file.habits.forEach { db.habitDao().insertKeep(it) }
                db.habitDao().insertLogsKeep(file.habitLogs)
                db.noteDao().insertKeep(file.notes)
                file.lists.forEach { db.listsDao().insertKeep(it) }
                db.listsDao().insertItemsKeep(file.listItems)
                db.calendarDao().insertKeep(file.events)
                file.categories.forEach { db.calendarDao().insertCategoryKeep(it) }
                file.slots.forEach { db.scheduleDao().insertSlotKeep(it) }
                file.entries.forEach { db.scheduleDao().insertEntryKeep(it) }
                file.presets.forEach { db.scheduleDao().insertPresetKeep(it) }
                db.fortuneDao().insertKeep(file.fortune)
                db.focusDao().insertKeep(file.focus)
            } else {
                val target = resolvedTarget
                wipeAccount(target)
                // persona
                file.accounts.firstOrNull()?.let { persona ->
                    db.accountDao().insertKeep(persona.copy(id = target))
                }
                // remap helper tables
                val habitMap = HashMap<Long, Long>()
                file.habits.forEach { old ->
                    val newId = db.habitDao().upsert(old.copy(id = 0, accountId = target))
                    habitMap[old.id] = newId
                }
                file.habitLogs.forEach {
                    habitMap[it.habitId]?.let { nid ->
                        db.habitDao().insertLog(HabitLogEntity(habitId = nid, accountId = target, epochDay = it.epochDay))
                    }
                }
                val listMap = HashMap<Long, Long>()
                file.lists.forEach { old ->
                    val newId = db.listsDao().upsert(old.copy(id = 0, accountId = target))
                    listMap[old.id] = newId
                }
                file.listItems.forEach {
                    listMap[it.listId]?.let { nid ->
                        db.listsDao().upsertItem(it.copy(id = 0, listId = nid, accountId = target))
                    }
                }
                val slotMap = HashMap<Long, Long>()
                file.slots.sortedBy { it.orderIndex }.forEach { old ->
                    val newId = db.scheduleDao().upsertSlot(old.copy(id = 0, accountId = target))
                    slotMap[old.id] = newId
                }
                file.entries.forEach {
                    slotMap[it.slotId]?.let { sid ->
                        db.scheduleDao().upsertEntry(it.copy(id = 0, slotId = sid, accountId = target))
                    }
                }
                file.presets.forEach { db.scheduleDao().upsertPreset(it.copy(id = 0, accountId = target)) }
                // Categories get NEW ids on import — build a remap table so events keep pointing
                // at the right category instead of dangling on the backup's stale ids.
                val categoryMap = HashMap<Long, Long>()
                file.categories.forEach { old ->
                    val newId = db.calendarDao().upsertCategory(old.copy(id = 0, accountId = target))
                    categoryMap[old.id] = newId
                }
                file.tasks.forEach { db.taskDao().upsert(it.copy(id = 0, accountId = target)) }
                file.goals.forEach { db.goalDao().upsert(it.copy(id = 0, accountId = target)) }
                file.notes.forEach { db.noteDao().upsert(it.copy(id = 0, accountId = target)) }
                file.events.forEach { old ->
                    val mappedCategory = old.categoryId?.let { categoryMap[it] }
                    db.calendarDao().upsert(old.copy(id = 0, accountId = target, categoryId = mappedCategory))
                }
                file.fortune.forEach { db.fortuneDao().upsert(it.copy(id = 0, accountId = target)) }
                file.focus.forEach {
                    db.focusDao().insert(it.copy(id = 0, accountId = target))
                }
            }
        }
        return true
    }

    private suspend fun wipeAccount(a: Long) {
        db.taskDao().deleteAllForAccount(a)
        db.goalDao().deleteAllForAccount(a)
        db.habitDao().deleteAllForAccount(a)
        db.habitDao().deleteAllLogsForAccount(a)
        db.noteDao().deleteAllForAccount(a)
        db.listsDao().deleteAllForAccount(a)
        db.listsDao().deleteAllItemsForAccount(a)
        db.calendarDao().deleteAllForAccount(a)
        db.calendarDao().deleteCategoriesForAccount(a)
        db.scheduleDao().deleteAllEntries(a)
        db.scheduleDao().deleteAllSlots(a)
        db.fortuneDao().deleteAllForAccount(a)
        db.focusDao().deleteAllForAccount(a)
    }

    private suspend fun wipeEverything() {
        db.taskDao().deleteEverything()
        db.goalDao().deleteEverything()
        db.habitDao().deleteLogsEverything()
        db.habitDao().deleteEverything()
        db.noteDao().deleteEverything()
        db.listsDao().deleteItemsEverything()
        db.listsDao().deleteEverything()
        db.calendarDao().deleteEverything()
        db.calendarDao().deleteCategoriesEverything()
        db.scheduleDao().deleteAllEntriesEverything()
        db.scheduleDao().deleteAllSlotsEverything()
        db.scheduleDao().deletePresetsEverything()
        db.fortuneDao().deleteEverything()
        db.focusDao().deleteEverything()
        db.accountDao().deleteEverything()
    }

    companion object {
        fun suggestedFileName(): String {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd-HHmm", java.util.Locale.US)
            return "nexora-backup-${fmt.format(java.util.Date())}.json"
        }
    }
}
