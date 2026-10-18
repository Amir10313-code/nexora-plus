package com.mahirex.nexoraplus.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.data.local.entity.GoalEntity
import com.mahirex.nexoraplus.data.local.entity.HabitEntity
import com.mahirex.nexoraplus.data.local.entity.ListItemEntity
import com.mahirex.nexoraplus.data.local.entity.NoteEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SearchResults(
    val events: List<CalEventEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val lists: List<ListItemEntity> = emptyList()
) {
    val isEmpty: Boolean get() = events.isEmpty() && tasks.isEmpty() && goals.isEmpty() &&
        habits.isEmpty() && notes.isEmpty() && lists.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(private val container: AppContainer) : ViewModel() {

    val query = MutableStateFlow("")

    private val activeId = container.settings.settings.map { it.activeAccountId }

    val results: StateFlow<SearchResults> = combine(query.debounce(200).map { it.trim() }, activeId) { q, id -> q to id }
        .flatMapLatest { (q, id) ->
            if (q.length < 2) {
                kotlinx.coroutines.flow.flowOf(SearchResults())
            } else {
                combine(
                    combine(
                        container.calendar.search(id, q),
                        container.tasks.search(id, q),
                        container.goals.search(id, q)
                    ) { e, t, g -> Triple(e, t, g) },
                    combine(
                        container.habits.search(id, q),
                        container.notes.search(id, q),
                        container.lists.lists(id).map { list -> list.filter { it.title.contains(q, ignoreCase = true) } }
                    ) { h, n, l -> Triple(h, n, l) }
                ) { (e, t, g), (h, n, l) ->
                    SearchResults(events = e, tasks = t, goals = g, habits = h, notes = n, lists = l)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())
}
