package com.mahirex.nexoraplus.di

import android.content.Context
import com.mahirex.nexoraplus.R
import com.mahirex.nexoraplus.data.local.NexoraDatabase
import com.mahirex.nexoraplus.data.repo.AccountRepository
import com.mahirex.nexoraplus.data.repo.BackupManager
import com.mahirex.nexoraplus.data.repo.CalendarRepository
import com.mahirex.nexoraplus.data.repo.FocusRepository
import com.mahirex.nexoraplus.data.repo.FortuneRepository
import com.mahirex.nexoraplus.data.repo.GoalRepository
import com.mahirex.nexoraplus.data.repo.HabitRepository
import com.mahirex.nexoraplus.data.repo.ListsRepository
import com.mahirex.nexoraplus.data.repo.NoteRepository
import com.mahirex.nexoraplus.data.repo.OccasionAdminRepository
import com.mahirex.nexoraplus.data.repo.ScheduleDefaults
import com.mahirex.nexoraplus.data.repo.ScheduleRepository
import com.mahirex.nexoraplus.data.repo.SettingsRepository
import com.mahirex.nexoraplus.data.repo.TaskRepository
import com.mahirex.nexoraplus.data.local.entity.EventCategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manual dependency container (keeps the build lean & predictable — no codegen DI).
 * Every screen gets its ViewModel through [com.mahirex.nexoraplus.ui.NexoraViewModelFactory].
 */
class AppContainer(val appContext: Context) {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val db: NexoraDatabase = NexoraDatabase.get(appContext)
    val settings = SettingsRepository(appContext)

    val accounts = AccountRepository(db)
    val tasks = TaskRepository(db)
    val goals = GoalRepository(db)
    val habits = HabitRepository(db)
    val notes = NoteRepository(db)
    val lists = ListsRepository(db)
    val calendar = CalendarRepository(db)
    val schedule = ScheduleRepository(db)
    val fortune = FortuneRepository(db)
    val focus = FocusRepository(db)
    val occasions = OccasionAdminRepository(db)
    val backup = BackupManager(db)

    /** First-run bootstrap: seed occasions + create the first account with categories & default slots. */
    fun bootstrap(onReady: (Long) -> Unit = {}) {
        appScope.launch {
            occasions.seedBundled()
            val current = settings.settings.first()
            var active = current.activeAccountId
            if (active == 0L || accounts.byId(active) == null) {
                val first = db.accountDao().observeAllSnapshot().firstOrNull()
                active = first?.id ?: run {
                    val id = accounts.create(
                        name = appContext.getString(R.string.default_account),
                        avatarKey = "🌟", gender = "u", colorHex = "#5B3FE0"
                    )
                    seedAccountData(id)
                    id
                }
                settings.setActiveAccount(active)
            }
            // Already inside appScope.launch (suspend context) — calling the suspend function
            // directly avoids runBlocking, which could starve/deadlock the shared IO pool.
            seedAccountData(active)
            onReady(active)
        }
    }

    /** Localized system categories + factory weekly slots for a brand-new account. */
    suspend fun seedAccountData(accountId: Long) {
        if (db.calendarDao().categoryCount(accountId) == 0) {
            val seeds = listOf(
                Triple(R.string.seed_personal, "🏠", "#5B3FE0"),
                Triple(R.string.seed_work, "💼", "#0891B2"),
                Triple(R.string.seed_study, "📚", "#7C5CFF"),
                Triple(R.string.seed_birthday, "🎂", "#DB2777"),
                Triple(R.string.seed_sport, "⚽", "#059669"),
                Triple(R.string.seed_other, "📌", "#D97706")
            )
            seeds.forEach { (res, emoji, color) ->
                db.calendarDao().upsertCategory(
                    EventCategoryEntity(
                        accountId = accountId,
                        name = appContext.getString(res),
                        emoji = emoji,
                        colorHex = color,
                        isSystem = true
                    )
                )
            }
        }
        if (db.scheduleDao().slotCount(accountId) == 0) {
            ScheduleDefaults.of(appContext, accountId).forEach { schedule.saveSlot(it) }
        }
    }

    suspend fun switchAccount(id: Long) {
        settings.setActiveAccount(id)
        accounts.touch(id)
        seedAccountData(id)
    }

    /** Current active account id (safe fallback to first account). */
    suspend fun activeIdCompat(): Long {
        val s = settings.settings.first()
        if (s.activeAccountId != 0L) return s.activeAccountId
        val first = db.accountDao().observeAllSnapshot().firstOrNull()?.id ?: 0L
        if (first != 0L) settings.setActiveAccount(first)
        return first
    }

    /** Safe full deletion of an account and every piece of its data. */
    suspend fun deleteAccountCascade(id: Long) {
        db.taskDao().deleteAllForAccount(id)
        db.goalDao().deleteAllForAccount(id)
        db.habitDao().deleteAllForAccount(id)
        db.habitDao().deleteAllLogsForAccount(id)
        db.noteDao().deleteAllForAccount(id)
        db.listsDao().deleteAllForAccount(id)
        db.listsDao().deleteAllItemsForAccount(id)
        db.calendarDao().deleteAllForAccount(id)
        db.scheduleDao().deleteAllEntries(id)
        db.scheduleDao().deleteAllSlots(id)
        db.fortuneDao().deleteAllForAccount(id)
        db.focusDao().deleteAllForAccount(id)
        db.calendarDao().deleteCategoriesForAccount(id)
        db.accountDao().deleteById(id)
        val remaining = db.accountDao().observeAllSnapshot()
        if (remaining.isNotEmpty() && settings.settings.first().activeAccountId == id) {
            settings.setActiveAccount(remaining.first().id)
        } else if (remaining.isEmpty()) {
            settings.setActiveAccount(0L)
        }
    }
}
