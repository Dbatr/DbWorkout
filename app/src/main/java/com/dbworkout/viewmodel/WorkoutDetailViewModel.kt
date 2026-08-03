package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WorkoutDetailUiState(
    val workout: Workout? = null,
    val isLoading: Boolean = true,
)

class WorkoutDetailViewModel(
    private val workoutId: Long,
    private val repository: WorkoutRepository,
) : ViewModel() {
    val uiState: StateFlow<WorkoutDetailUiState> = repository.observeWorkout(workoutId)
        .map { WorkoutDetailUiState(workout = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutDetailUiState())

    suspend fun delete() = repository.deleteWorkout(workoutId)
}
