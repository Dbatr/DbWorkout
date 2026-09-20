package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.DuplicateRecordDateException
import com.dbworkout.data.repository.WorkoutRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecordEditorUiState(
    val exerciseId: Long,
    val recordId: Long? = null,
    val exerciseName: String = "",
    val weight: String = "",
    val date: LocalDate = LocalDate.now(),
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val weightError: Boolean = false,
    val duplicateDateError: Boolean = false,
    val saveError: Boolean = false,
)

class RecordEditorViewModel(
    private val exerciseId: Long,
    private val recordId: Long?,
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordEditorUiState(exerciseId = exerciseId, recordId = recordId))
    val uiState: StateFlow<RecordEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = repository.getExercise(exerciseId)
            val record = recordId?.let { repository.getRecord(it) }
            _uiState.value = _uiState.value.copy(
                exerciseName = exercise?.name.orEmpty(),
                weight = record?.weightKg?.let(::formatWeight).orEmpty(),
                date = record?.date ?: LocalDate.now(),
                notes = record?.notes.orEmpty(),
                isLoading = false,
                saveError = exercise == null || (recordId != null && record == null),
            )
        }
    }

    fun setWeight(value: String) {
        _uiState.value = _uiState.value.copy(
            weight = value,
            weightError = false,
            duplicateDateError = false,
            saveError = false,
        )
    }

    fun setDate(value: LocalDate) {
        _uiState.value = _uiState.value.copy(date = value, duplicateDateError = false, saveError = false)
    }

    fun setNotes(value: String) { _uiState.value = _uiState.value.copy(notes = value) }

    suspend fun save(): Result<Long> {
        val state = _uiState.value
        val parsed = state.weight.trim().replace(',', '.').toDoubleOrNull()
        if (parsed == null || parsed <= 0.0) {
            _uiState.value = state.copy(weightError = true, duplicateDateError = false, saveError = false)
            return Result.failure(IllegalArgumentException())
        }
        _uiState.value = state.copy(
            isSaving = true,
            weightError = false,
            duplicateDateError = false,
            saveError = false,
        )
        return runCatching {
            repository.saveRecord(state.recordId, state.exerciseId, parsed, state.date, state.notes)
        }.onSuccess {
            _uiState.value = _uiState.value.copy(isSaving = false)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                duplicateDateError = error is DuplicateRecordDateException,
                saveError = error !is DuplicateRecordDateException,
            )
        }
    }
}

private fun formatWeight(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
