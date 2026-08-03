package com.dbworkout.model

import java.time.LocalDate

data class Exercise(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val isCustom: Boolean,
    val notes: String?,
)

data class WorkoutSet(
    val id: Long,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double?,
)

data class WorkoutExercise(
    val id: Long,
    val exercise: Exercise,
    val orderIndex: Int,
    val sets: List<WorkoutSet>,
)

data class Workout(
    val id: Long,
    val date: LocalDate,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val exercises: List<WorkoutExercise>,
)

data class WorkoutListItem(
    val id: Long,
    val date: LocalDate,
    val exerciseCount: Int,
)

data class WorkoutDraft(
    val id: Long?,
    val date: LocalDate,
    val notes: String?,
    val exercises: List<WorkoutExerciseDraft>,
)

data class WorkoutExerciseDraft(
    val exerciseId: Long,
    val orderIndex: Int,
    val sets: List<WorkoutSetDraft>,
)

data class WorkoutSetDraft(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Double?,
)
