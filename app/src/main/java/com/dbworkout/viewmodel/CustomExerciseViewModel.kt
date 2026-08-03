package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.ExerciseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomExerciseUiState(
    val id: Long? = null,
    val name: String = "",
    val category: ExerciseCategory = ExerciseCategory.OTHER,
    val notes: String = "",
    val isLoading: Boolean = false,
    val nameError: Boolean = false,
    val saveError: Boolean = false,
)

class CustomExerciseViewModel(
    private val exerciseId: Long?,
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomExerciseUiState(id = exerciseId, isLoading = exerciseId != null))
    val uiState: StateFlow<CustomExerciseUiState> = _uiState.asStateFlow()

    init {
        if (exerciseId != null) {
            viewModelScope.launch {
                val exercise = repository.getExercise(exerciseId)
                _uiState.value = if (exercise?.isCustom == true) {
                    CustomExerciseUiState(
                        id = exercise.id,
                        name = exercise.name,
                        category = exercise.category,
                        notes = exercise.notes.orEmpty(),
                    )
                } else {
                    CustomExerciseUiState(saveError = true)
                }
            }
        }
    }

    fun setName(value: String) { _uiState.value = _uiState.value.copy(name = value, nameError = false, saveError = false) }
    fun setCategory(value: ExerciseCategory) { _uiState.value = _uiState.value.copy(category = value) }
    fun setNotes(value: String) { _uiState.value = _uiState.value.copy(notes = value) }

    suspend fun save(): Result<Long> {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = true)
            return Result.failure(IllegalArgumentException())
        }
        return runCatching {
            repository.saveCustomExercise(state.id, state.name, state.category, state.notes)
        }.onFailure {
            _uiState.value = _uiState.value.copy(saveError = true)
        }
    }
}
