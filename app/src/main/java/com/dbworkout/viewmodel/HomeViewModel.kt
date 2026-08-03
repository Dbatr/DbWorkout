package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.WorkoutListItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val workouts: List<WorkoutListItem> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val today = LocalDate.now()
    private val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val weekEnd = weekStart.plusDays(6)

    val uiState: StateFlow<HomeUiState> = repository.observeWorkouts(weekStart, weekEnd)
        .map { HomeUiState(workouts = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    suspend fun workoutIdForToday(): Long? = repository.findWorkoutId(LocalDate.now())
}
