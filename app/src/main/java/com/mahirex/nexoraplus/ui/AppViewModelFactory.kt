package com.mahirex.nexoraplus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mahirex.nexoraplus.di.AppContainer
import com.mahirex.nexoraplus.ui.screens.accounts.AccountsViewModel
import com.mahirex.nexoraplus.ui.screens.calendar.CalendarViewModel
import com.mahirex.nexoraplus.ui.screens.fortune.FortuneViewModel
import com.mahirex.nexoraplus.ui.screens.focus.FocusViewModel
import com.mahirex.nexoraplus.ui.screens.home.HomeViewModel
import com.mahirex.nexoraplus.ui.screens.lists.ListsViewModel
import com.mahirex.nexoraplus.ui.screens.search.SearchViewModel
import com.mahirex.nexoraplus.ui.screens.settings.SettingsViewModel
import com.mahirex.nexoraplus.ui.screens.stats.StatsViewModel
import com.mahirex.nexoraplus.ui.screens.tasks.TasksViewModel
import com.mahirex.nexoraplus.ui.screens.weekly.WeeklyViewModel

/** Single factory wiring every ViewModel to the shared [AppContainer]. */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(container) as T
        modelClass.isAssignableFrom(WeeklyViewModel::class.java) -> WeeklyViewModel(container) as T
        modelClass.isAssignableFrom(TasksViewModel::class.java) -> TasksViewModel(container) as T
        modelClass.isAssignableFrom(ListsViewModel::class.java) -> ListsViewModel(container) as T
        modelClass.isAssignableFrom(CalendarViewModel::class.java) -> CalendarViewModel(container) as T
        modelClass.isAssignableFrom(FortuneViewModel::class.java) -> FortuneViewModel(container) as T
        modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(container) as T
        modelClass.isAssignableFrom(AccountsViewModel::class.java) -> AccountsViewModel(container) as T
        modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(container) as T
        modelClass.isAssignableFrom(StatsViewModel::class.java) -> StatsViewModel(container) as T
        modelClass.isAssignableFrom(FocusViewModel::class.java) -> FocusViewModel(container) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
