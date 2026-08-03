package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.DuplicateWorkoutDateException
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.Exercise
import com.dbworkout.model.WorkoutDraft
import com.dbworkout.model.WorkoutExerciseDraft
import com.dbworkout.model.WorkoutSetDraft
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DraftSetUi(
    val key: Long,
    val reps: String = "",
    val weightKg: String = "",
)

data class DraftExerciseUi(
    val key: Long,
    val exerciseId: Long,
    val name: String,
    val sets: List<DraftSetUi>,
)

enum class WorkoutEditorError {
    NO_EXERCISES,
    INVALID_SETS,
    DUPLICATE_DATE,
    SAVE_FAILED,
}

data class WorkoutEditorUiState(
    val workoutId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val notes: String = "",
    val exercises: List<DraftExerciseUi> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: WorkoutEditorError? = null,
)

sealed interface WorkoutEditorEvent {
    data class Saved(val workoutId: Long) : WorkoutEditorEvent
}

class WorkoutEditorViewModel(
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutEditorUiState(isLoading = false))
    val uiState: StateFlow<WorkoutEditorUiState> = _uiState.asStateFlow()

    private val eventsChannel = Channel<WorkoutEditorEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    private var loadJob: Job? = null

    fun start(requestedWorkoutId: Long?) {
        loadJob?.cancel()
        if (requestedWorkoutId == null) {
            _uiState.value = WorkoutEditorUiState(isLoading = false)
        } else {
            _uiState.value = WorkoutEditorUiState(workoutId = requestedWorkoutId, isLoading = true)
            loadJob = viewModelScope.launch {
                val workout = repository.getWorkout(requestedWorkoutId)
                _uiState.value = if (workout == null) {
                    WorkoutEditorUiState(isLoading = false, error = WorkoutEditorError.SAVE_FAILED)
                } else {
                    WorkoutEditorUiState(
                        workoutId = workout.id,
                        date = workout.date,
                        notes = workout.notes.orEmpty(),
                        exercises = workout.exercises.map { workoutExercise ->
                            DraftExerciseUi(
                                key = workoutExercise.id,
                                exerciseId = workoutExercise.exercise.id,
                                name = workoutExercise.exercise.name,
                                sets = workoutExercise.sets.map { set ->
                                    DraftSetUi(
                                        key = set.id,
                                        reps = set.reps.toString(),
                                        weightKg = set.weightKg?.toInputString().orEmpty(),
                                    )
                                },
                            )
                        },
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun setDate(date: LocalDate) = _uiState.update { it.copy(date = date, error = null) }
    fun setNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun addExercise(exercise: Exercise): Long {
        val existing = _uiState.value.exercises.firstOrNull { it.exerciseId == exercise.id }
        if (existing != null) return existing.key
        val key = nextKey()
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises + DraftExerciseUi(
                    key = key,
                    exerciseId = exercise.id,
                    name = exercise.name,
                    sets = listOf(DraftSetUi(key = nextKey())),
                ),
                error = null,
            )
        }
        return key
    }

    fun removeExercise(key: Long) = _uiState.update { state ->
        state.copy(exercises = state.exercises.filterNot { it.key == key })
    }

    fun moveExercise(key: Long, direction: Int) = _uiState.update { state ->
        val index = state.exercises.indexOfFirst { it.key == key }
        val target = index + direction
        if (index < 0 || target !in state.exercises.indices) return@update state
        val reordered = state.exercises.toMutableList()
        val item = reordered.removeAt(index)
        reordered.add(target, item)
        state.copy(exercises = reordered)
    }

    fun updateSet(exerciseKey: Long, setKey: Long, reps: String? = null, weight: String? = null) {
        _uiState.update { state ->
            state.copy(
                exercises = state.exercises.map { exercise ->
                    if (exercise.key != exerciseKey) exercise else exercise.copy(
                        sets = exercise.sets.map { set ->
                            if (set.key != setKey) set else set.copy(
                                reps = reps ?: set.reps,
                                weightKg = weight ?: set.weightKg,
                            )
                        },
                    )
                },
                error = null,
            )
        }
    }

    fun addSet(exerciseKey: Long) = _uiState.update { state ->
        state.copy(exercises = state.exercises.map { exercise ->
            if (exercise.key != exerciseKey) exercise else {
                val previous = exercise.sets.lastOrNull()
                exercise.copy(
                    sets = exercise.sets + DraftSetUi(
                        key = nextKey(),
                        reps = previous?.reps.orEmpty(),
                        weightKg = previous?.weightKg.orEmpty(),
                    ),
                )
            }
        })
    }

    fun removeSet(exerciseKey: Long, setKey: Long) = _uiState.update { state ->
        state.copy(exercises = state.exercises.map { exercise ->
            if (exercise.key != exerciseKey || exercise.sets.size <= 1) exercise
            else exercise.copy(sets = exercise.sets.filterNot { it.key == setKey })
        })
    }

    fun exercise(key: Long): DraftExerciseUi? = _uiState.value.exercises.firstOrNull { it.key == key }

    fun exerciseHasValidSets(key: Long): Boolean {
        val exercise = exercise(key) ?: return false
        val valid = exercise.sets.isNotEmpty() && exercise.sets.all(DraftSetUi::isValid)
        if (!valid) _uiState.update { it.copy(error = WorkoutEditorError.INVALID_SETS) }
        return valid
    }

    fun save() {
        val state = _uiState.value
        val error = when {
            state.exercises.isEmpty() -> WorkoutEditorError.NO_EXERCISES
            state.exercises.any { exercise -> exercise.sets.isEmpty() || exercise.sets.any { !it.isValid() } } ->
                WorkoutEditorError.INVALID_SETS
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val current = _uiState.value
            runCatching {
                repository.saveWorkout(
                    WorkoutDraft(
                        id = current.workoutId,
                        date = current.date,
                        notes = current.notes,
                        exercises = current.exercises.mapIndexed { exerciseIndex, exercise ->
                            WorkoutExerciseDraft(
                                exerciseId = exercise.exerciseId,
                                orderIndex = exerciseIndex,
                                sets = exercise.sets.mapIndexed { setIndex, set ->
                                    WorkoutSetDraft(
                                        setNumber = setIndex + 1,
                                        reps = set.reps.toInt(),
                                        weightKg = set.weightKg.toWeightOrNull(),
                                    )
                                },
                            )
                        },
                    ),
                )
            }.onSuccess { id ->
                _uiState.update { it.copy(workoutId = id, isSaving = false) }
                eventsChannel.send(WorkoutEditorEvent.Saved(id))
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = if (throwable is DuplicateWorkoutDateException) {
                            WorkoutEditorError.DUPLICATE_DATE
                        } else {
                            WorkoutEditorError.SAVE_FAILED
                        },
                    )
                }
            }
        }
    }

    private var generatedKey = -System.nanoTime()
    private fun nextKey(): Long = generatedKey--
}

fun DraftSetUi.isValid(): Boolean {
    val parsedReps = reps.toIntOrNull()
    val parsedWeight = weightKg.toWeightOrNull()
    return parsedReps != null && parsedReps > 0 && (weightKg.isBlank() || parsedWeight != null && parsedWeight >= 0.0)
}

private fun String.toWeightOrNull(): Double? = trim().replace(',', '.').takeIf(String::isNotEmpty)?.toDoubleOrNull()

private fun Double.toInputString(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
