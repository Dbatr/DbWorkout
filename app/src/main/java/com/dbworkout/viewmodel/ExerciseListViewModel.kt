package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.Exercise
import com.dbworkout.model.ExerciseCategory
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExerciseListUiState(
    val exercises: List<Exercise> = emptyList(),
    val query: String = "",
    val category: ExerciseCategory? = null,
    val isLoading: Boolean = true,
)

class ExerciseListViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ExerciseCategory?>(null)

    val uiState: StateFlow<ExerciseListUiState> = combine(
        repository.observeExercises(),
        query,
        category,
    ) { exercises, search, selectedCategory ->
        val normalized = search.trim().lowercase(Locale.getDefault())
        ExerciseListUiState(
            exercises = exercises.filter { exercise ->
                (selectedCategory == null || exercise.category == selectedCategory) &&
                    (normalized.isEmpty() || exercise.name.lowercase(Locale.getDefault()).contains(normalized))
            },
            query = search,
            category = selectedCategory,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExerciseListUiState())

    fun setQuery(value: String) { query.value = value }
    fun setCategory(value: ExerciseCategory?) { category.value = value }
    fun resetFilters() { query.value = ""; category.value = null }

    suspend fun deleteCustom(id: Long): Result<Unit> = runCatching {
        check(repository.deleteCustomExercise(id))
    }
}
