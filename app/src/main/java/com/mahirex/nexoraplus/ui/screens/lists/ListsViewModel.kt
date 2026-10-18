package com.mahirex.nexoraplus.ui.screens.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.data.local.entity.ChecklistItemEntity
import com.mahirex.nexoraplus.data.local.entity.ListItemEntity
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListsViewModel(private val container: AppContainer) : ViewModel() {

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val lists: StateFlow<List<ListItemEntity>> = activeId
        .flatMapLatest { id -> container.lists.lists(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val expandedId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    val expanded: StateFlow<Long?> = expandedId

    val items: StateFlow<List<ChecklistItemEntity>> = expandedId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else container.lists.items(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleExpand(id: Long?) {
        expandedId.value = if (expandedId.value == id) null else id
    }

    fun save(list: ListItemEntity) = viewModelScope.launch {
        val id = container.activeIdCompat()
        container.lists.save(list.copy(accountId = if (list.accountId == 0L) id else list.accountId))
    }

    fun deleteList(id: Long) = viewModelScope.launch {
        container.lists.deleteList(id)
        if (expandedId.value == id) expandedId.value = null
    }

    fun addItem(listId: Long, title: String) = viewModelScope.launch {
        val id = container.activeIdCompat()
        val order = items.value.size
        container.lists.saveItem(
            ChecklistItemEntity(listId = listId, accountId = id, title = title, orderIndex = order)
        )
    }

    fun toggleItem(item: ChecklistItemEntity) = viewModelScope.launch {
        container.lists.setItemDone(item.id, !item.isDone)
    }

    fun deleteItem(id: Long) = viewModelScope.launch { container.lists.deleteItem(id) }
}
