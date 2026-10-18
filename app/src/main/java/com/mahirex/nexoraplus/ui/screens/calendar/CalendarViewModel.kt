package com.mahirex.nexoraplus.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.data.local.entity.EventCategoryEntity
import com.mahirex.nexoraplus.data.local.entity.OccasionEntity
import com.mahirex.nexoraplus.data.repo.AppSettings
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(private val container: AppContainer) : ViewModel() {

    val today: Long = CalendarConverter.todayEpochDay()

    private val selectedDay = MutableStateFlow(today)
    val selected: StateFlow<Long> = selectedDay

    /** month anchor: any epochDay inside the displayed month */
    private val monthAnchor = MutableStateFlow(today)
    val anchor: StateFlow<Long> = monthAnchor

    val calendarType: StateFlow<Int> = container.settings.settings.map { it.calendarType }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val eventsOfMonth: StateFlow<List<CalEventEntity>> = combine(activeId, monthAnchor, calendarType) { id, a, cal -> Triple(id, a, cal) }
        .flatMapLatest { (id, a, cal) ->
            val from: Long
            val to: Long
            if (cal == 0) {
                val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(a))
                from = CalendarConverter.jalaliToGregorian(j.year, j.month, 1).toEpochDay()
                val len = CalendarConverter.jalaliMonthLength(j.year, j.month)
                to = CalendarConverter.jalaliToGregorian(j.year, j.month, len).toEpochDay()
            } else {
                val g = LocalDate.ofEpochDay(a)
                from = g.withDayOfMonth(1).toEpochDay()
                to = g.withDayOfMonth(g.lengthOfMonth()).toEpochDay()
            }
            container.calendar.eventsBetween(id, from, to)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventsOfSelected: StateFlow<List<CalEventEntity>> = combine(activeId, selectedDay) { id, d -> id to d }
        .flatMapLatest { (id, d) -> container.calendar.eventsForDay(id, d) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val occasionsOfSelected: StateFlow<List<OccasionEntity>> =
        combine(selectedDay, container.settings.settings.map { it.occasionVersion }) { d, _ -> d }
            .flatMapLatest { d ->
                flow {
                    val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(d))
                    val solar = container.calendar.occasionsForJalaliDay(j).first()
                    emit(solar + container.calendar.lunarOccasionsFor(j))
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<EventCategoryEntity>> = activeId
        .flatMapLatest { id -> container.calendar.categories(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun select(day: Long) { selectedDay.value = day }
    fun moveMonth(delta: Int) {
        val cal = calendarType.value
        monthAnchor.value = if (cal == 0) {
            val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(monthAnchor.value))
            var m = j.month + delta
            var y = j.year
            if (m > 12) { m = 1; y++ }
            if (m < 1) { m = 12; y-- }
            CalendarConverter.jalaliToGregorian(y, m, 1).toEpochDay()
        } else {
            LocalDate.ofEpochDay(monthAnchor.value).plusMonths(delta.toLong()).toEpochDay()
        }
    }

    fun goToday() {
        selectedDay.value = today
        monthAnchor.value = today
    }

    fun save(event: CalEventEntity) = viewModelScope.launch {
        val id = container.activeIdCompat()
        container.calendar.save(event.copy(accountId = if (event.accountId == 0L) id else event.accountId))
    }

    fun delete(id: Long) = viewModelScope.launch { container.calendar.delete(id) }
    fun pin(event: CalEventEntity) = viewModelScope.launch { container.calendar.setPinned(event.id, !event.isPinned) }

    fun saveCategory(c: EventCategoryEntity) = viewModelScope.launch {
        val id = container.activeIdCompat()
        container.calendar.saveCategory(c.copy(accountId = if (c.accountId == 0L) id else c.accountId))
    }

    fun deleteCategory(c: EventCategoryEntity) = viewModelScope.launch { container.calendar.deleteCategory(c.id) }
}
