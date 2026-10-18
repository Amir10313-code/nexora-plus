package com.mahirex.nexoraplus.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.data.local.entity.AccountEntity
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(private val container: AppContainer) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = container.accounts.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val active: StateFlow<AccountEntity?> = accounts
        .combine(activeId) { list, id -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun create(name: String, avatar: String, gender: String, color: String, onDone: (Boolean) -> Unit) =
        viewModelScope.launch {
            if (accounts.value.size >= 3) {
                onDone(false)
                return@launch
            }
            val id = container.accounts.create(name, avatar, gender, color)
            container.seedAccountData(id)
            onDone(true)
        }

    fun update(account: AccountEntity, onDone: () -> Unit = {}) = viewModelScope.launch {
        container.accounts.update(account)
        onDone()
    }

    fun switch(id: Long, onDone: (AccountEntity?) -> Unit = {}) = viewModelScope.launch {
        container.switchAccount(id)
        onDone(container.accounts.byId(id))
    }

    fun delete(account: AccountEntity, onDone: () -> Unit = {}) = viewModelScope.launch {
        container.deleteAccountCascade(account.id)
        onDone()
    }
}
