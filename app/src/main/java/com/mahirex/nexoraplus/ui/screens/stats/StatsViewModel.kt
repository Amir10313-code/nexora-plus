package com.mahirex.nexoraplus.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatsData(
    val tasksTotal: Int = 0,
    val tasksDone: Int = 0,
    val eventsTotal: Int = 0,
    val goalsTotal: Int = 0,
    val listsTotal: Int = 0,
    val focusMinutes: Int = 0,
    val bestHabit: String = "",
    val bestStreak: Int = 0,
    val weekTasks: List<Int> = emptyList() // done counts per day (7 days, oldest→today)
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(private val container: AppContainer) : ViewModel() {

    val today: Long = CalendarConverter.todayEpochDay()

    private val activeId = container.settings.settings.map { it.activeAccountId }

    val stats: StateFlow<StatsData> = activeId.flatMapLatest { id ->
        if (id <= 0) kotlinx.coroutines.flow.flowOf(StatsData())
        else combine(
            combine(
                container.tasks.tasks(id, null).map { list -> list.size to list.count { it.isDone } },
                container.calendar.count(id),
                container.goals.goals(id).map { it.size }
            ) { tc, ev, gl -> Triple(tc, ev, gl) },
            combine(
                container.lists.lists(id).map { it.size },
                container.focus.total(id)
            ) { ls, focus -> ls to focus },
            container.habits.habits(id),
            container.habits.logsSince(id, today - 7)
        ) { core, extras, habits, logs ->
            val (tc, ev, gl) = core
            val (ls, focus) = extras
            val best = habits.maxByOrNull { h -> container.habits.streak(logs, h.id, today) }
            val bestStreak = best?.let { container.habits.streak(logs, it.id, today) } ?: 0
            val week = (6 downTo 0).map { back ->
                logs.count { it.epochDay == today - back }
            }
            StatsData(
                tasksTotal = tc.first,
                tasksDone = tc.second,
                eventsTotal = ev,
                goalsTotal = gl,
                listsTotal = ls,
                focusMinutes = focus,
                bestHabit = best?.title ?: "",
                bestStreak = bestStreak,
                weekTasks = week
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())
}
