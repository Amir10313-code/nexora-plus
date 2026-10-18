package com.mahirex.nexoraplus.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.data.fortune.FortuneEngine
import com.mahirex.nexoraplus.data.fortune.DailyFortune
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.data.local.entity.CalEventEntity
import com.mahirex.nexoraplus.data.local.entity.OccasionEntity
import com.mahirex.nexoraplus.data.local.entity.TaskEntity
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.repo.AppSettings
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val settings = container.settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val today: Long = CalendarConverter.todayEpochDay()

    val activeId: StateFlow<Long> = settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val account: StateFlow<AccountEntity?> = activeId
        .flatMapLatest { id ->
            container.accounts.accounts.map { list -> list.firstOrNull { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fortune: StateFlow<DailyFortune> = activeId
        .map { id -> FortuneEngine.build(today, id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FortuneEngine.build(today, 0L))

    val occasions: StateFlow<List<OccasionEntity>> =
        settings.map { it.occasionVersion }
            .flatMapLatest {
                flow {
                    val j = CalendarConverter.gregorianToJalali(LocalDate.ofEpochDay(today))
                    val solar = container.calendar.occasionsForJalaliDay(j).first()
                    val lunar = container.calendar.lunarOccasionsFor(j)
                    emit(solar + lunar)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventsToday: StateFlow<List<CalEventEntity>> = activeId
        .flatMapLatest { id -> container.calendar.eventsForDay(id, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasksToday: StateFlow<List<TaskEntity>> = activeId
        .flatMapLatest { id -> container.tasks.forDay(id, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextCountdown: StateFlow<CalEventEntity?> = activeId
        .flatMapLatest { id -> container.calendar.nextCountdown(id, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch { container.tasks.setDone(task.id, !task.isDone) }
    }
}
