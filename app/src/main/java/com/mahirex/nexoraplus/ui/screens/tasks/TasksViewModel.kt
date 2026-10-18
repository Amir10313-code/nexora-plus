package com.mahirex.nexoraplus.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.local.entity.GoalEntity
import com.mahirex.nexoraplus.data.local.entity.HabitEntity
import com.mahirex.nexoraplus.data.local.entity.HabitLogEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, OPEN, DONE }

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(private val container: AppContainer) : ViewModel() {

    val today: Long = CalendarConverter.todayEpochDay()

    private val filter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = filter

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val tasks: StateFlow<List<TaskEntity>> =
        combine(activeId, filter) { id, f -> id to f }
            .flatMapLatest { (id, f) ->
                when (f) {
                    TaskFilter.ALL -> container.tasks.tasks(id, null)
                    TaskFilter.OPEN -> container.tasks.tasks(id, false)
                    TaskFilter.DONE -> container.tasks.tasks(id, true)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = activeId
        .flatMapLatest { id -> container.goals.goals(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = activeId
        .flatMapLatest { id -> container.habits.habits(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** logs of the last 35 days for streaks & weekly dots */
    val logs: StateFlow<List<HabitLogEntity>> = activeId
        .flatMapLatest { id -> container.habits.logsSince(id, today - 35) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(f: TaskFilter) { filter.value = f }

    fun saveTask(t: TaskEntity) = viewModelScope.launch {
        val id = container.activeIdCompat()
        container.tasks.save(t.copy(accountId = if (t.accountId == 0L) id else t.accountId))
    }

    fun deleteTask(id: Long) = viewModelScope.launch { container.tasks.delete(id) }
    fun toggleTask(t: TaskEntity) = viewModelScope.launch { container.tasks.setDone(t.id, !t.isDone) }
    fun pinTask(t: TaskEntity) = viewModelScope.launch { container.tasks.setPinned(t.id, !t.isPinned) }

    fun saveGoal(g: GoalEntity) = viewModelScope.launch {
        val id = container.activeIdCompat()
        container.goals.save(g.copy(accountId = if (g.accountId == 0L) id else g.accountId))
    }

    fun deleteGoal(id: Long) = viewModelScope.launch { container.goals.delete(id) }
    fun pinGoal(g: GoalEntity) = viewModelScope.launch { container.goals.setPinned(g.id, !g.isPinned) }

    fun saveHabit(h: HabitEntity) = viewModelScope.launch {
        val id = container.activeIdCompat()
        container.habits.save(h.copy(accountId = if (h.accountId == 0L) id else h.accountId))
    }

    fun deleteHabit(h: HabitEntity) = viewModelScope.launch { container.habits.delete(h) }
    fun toggleHabitDay(habitId: Long, epochDay: Long) = viewModelScope.launch {
        val id = activeId.value
        if (id > 0) container.habits.toggle(habitId, id, epochDay)
    }

    fun streakOf(habitId: Long): Int =
        container.habits.streak(logs.value, habitId, today)
}
