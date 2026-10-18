package com.mahirex.nexoraplus.ui.screens.fortune

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.core.calendar.CalendarConverter
import com.mahirex.nexoraplus.data.fortune.DailyFortune
import com.mahirex.nexoraplus.data.fortune.FortuneEngine
import com.mahirex.nexoraplus.data.local.entity.FortuneHistoryEntity
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FortuneViewModel(private val container: AppContainer) : ViewModel() {

    val today: Long = CalendarConverter.todayEpochDay()

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val fortune: StateFlow<DailyFortune> = activeId
        .map { id -> FortuneEngine.build(today, id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FortuneEngine.build(today, 0L))

    val history: StateFlow<List<FortuneHistoryEntity>> = activeId
        .flatMapLatest { id -> container.fortune.history(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Records today's fortune in history exactly once per day. */
    fun recordToday() = viewModelScope.launch {
        val id = activeId.value
        if (id > 0) container.fortune.record(id, fortune.value)
    }
}
