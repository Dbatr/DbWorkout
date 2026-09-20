package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.Exercise
import com.dbworkout.model.ExerciseRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecordHistoryUiState(
    val exerciseName: String = "",
    val records: List<ExerciseRecord> = emptyList(),
    val isLoading: Boolean = true,
)

class RecordHistoryViewModel(
    exerciseId: Long,
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val exercise = MutableStateFlow<Exercise?>(null)

    val uiState: StateFlow<RecordHistoryUiState> = combine(
        repository.observeRecordsForExercise(exerciseId),
        exercise,
    ) { records, current ->
        RecordHistoryUiState(
            exerciseName = current?.name.orEmpty(),
            records = records,
            isLoading = current == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordHistoryUiState())

    init {
        viewModelScope.launch {
            exercise.value = repository.getExercise(exerciseId)
        }
    }

    suspend fun delete(id: Long): Result<Unit> = runCatching { repository.deleteRecord(id) }
}
