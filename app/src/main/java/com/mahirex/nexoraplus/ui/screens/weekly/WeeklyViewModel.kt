package com.mahirex.nexoraplus.ui.screens.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahirex.nexoraplus.data.local.entity.ScheduleEntryEntity
import com.mahirex.nexoraplus.data.local.entity.SchedulePresetEntity
import com.mahirex.nexoraplus.data.local.entity.ScheduleSlotEntity
import com.mahirex.nexoraplus.data.repo.AppSettings
import com.mahirex.nexoraplus.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PresetSlot(val title: String, val start: Int, val end: Int, val colorHex: String)

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyViewModel(private val container: AppContainer) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    val activeId: StateFlow<Long> = container.settings.settings.map { it.activeAccountId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val slots: StateFlow<List<ScheduleSlotEntity>> = activeId
        .flatMapLatest { id -> container.schedule.slots(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entries: StateFlow<List<ScheduleEntryEntity>> = activeId
        .flatMapLatest { id -> container.schedule.entries(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presets: StateFlow<List<SchedulePresetEntity>> = activeId
        .flatMapLatest { id -> container.schedule.presets(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val time24: StateFlow<Boolean> = container.settings.settings.map { it.time24 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun saveSlot(slot: ScheduleSlotEntity) = viewModelScope.launch {
        val id = if (slot.accountId == 0L) activeId.value else slot.accountId
        container.schedule.saveSlot(slot.copy(accountId = id))
    }

    fun deleteSlot(slot: ScheduleSlotEntity) = viewModelScope.launch {
        container.schedule.deleteSlot(slot)
    }

    fun saveEntry(entry: ScheduleEntryEntity) = viewModelScope.launch {
        val id = if (entry.accountId == 0L) activeId.value else entry.accountId
        container.schedule.saveEntry(entry.copy(accountId = id))
    }

    fun deleteEntry(id: Long) = viewModelScope.launch {
        container.schedule.deleteEntry(id)
    }

    fun moveSlot(slot: ScheduleSlotEntity, delta: Int) = viewModelScope.launch {
        val list = slots.value.sortedBy { it.orderIndex }.toMutableList()
        val idx = list.indexOfFirst { it.id == slot.id }
        if (idx < 0) return@launch
        val target = idx + delta
        if (target !in list.indices) return@launch
        val swapped = list[target]
        list[target] = slot.copy(orderIndex = swapped.orderIndex)
        list[idx] = swapped.copy(orderIndex = slot.orderIndex)
        container.schedule.saveSlot(list[target])
        container.schedule.saveSlot(list[idx])
    }

    fun resetDefaults() = viewModelScope.launch {
        val id = activeId.value
        if (id > 0) container.schedule.applySlots(id, com.mahirex.nexoraplus.data.repo.ScheduleDefaults.of(container.appContext, id))
    }

    fun saveAsPreset(name: String) = viewModelScope.launch {
        val id = activeId.value
        if (id <= 0) return@launch
        val data = slots.value.sortedBy { it.orderIndex }
            .map { PresetSlot(it.title, it.startMinutes, it.endMinutes, it.colorHex) }
        container.schedule.savePreset(
            SchedulePresetEntity(accountId = id, name = name, slotsJson = json.encodeToString(data))
        )
    }

    fun applyPreset(preset: SchedulePresetEntity) = viewModelScope.launch {
        val id = activeId.value
        if (id <= 0) return@launch
        val data = try {
            json.decodeFromString<List<PresetSlot>>(preset.slotsJson)
        } catch (e: Exception) {
            return@launch
        }
        container.schedule.applySlots(
            id,
            data.mapIndexed { i, s ->
                ScheduleSlotEntity(
                    accountId = id, title = s.title,
                    startMinutes = s.start, endMinutes = s.end,
                    colorHex = s.colorHex, orderIndex = i
                )
            }
        )
    }

    fun deletePreset(preset: SchedulePresetEntity) = viewModelScope.launch {
        container.schedule.deletePreset(preset.id)
    }
}
