package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.RecordListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecordListUiState(
    val items: List<RecordListItem> = emptyList(),
    val isLoading: Boolean = true,
)

class RecordListViewModel(repository: WorkoutRepository) : ViewModel() {
    val uiState: StateFlow<RecordListUiState> = repository.observeRecordExercises()
        .map { items -> RecordListUiState(items = items, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordListUiState())
}
