package com.dbworkout.data.repository

import androidx.room.withTransaction
import com.dbworkout.data.local.dao.ExerciseDao
import com.dbworkout.data.local.dao.RecordDao
import com.dbworkout.data.local.dao.WorkoutDao
import com.dbworkout.data.local.database.DbWorkoutDatabase
import com.dbworkout.data.local.entity.ExerciseEntity
import com.dbworkout.data.local.entity.ExerciseSetEntity
import com.dbworkout.data.local.entity.RecordEntity
import com.dbworkout.data.local.entity.WorkoutEntity
import com.dbworkout.data.local.entity.WorkoutExerciseEntity
import com.dbworkout.data.local.entity.WorkoutWithExercises
import com.dbworkout.model.Exercise
import com.dbworkout.model.ExerciseCategory
import com.dbworkout.model.ExerciseRecord
import com.dbworkout.model.RecordListItem
import com.dbworkout.model.Workout
import com.dbworkout.model.WorkoutDraft
import com.dbworkout.model.WorkoutExercise
import com.dbworkout.model.WorkoutListItem
import com.dbworkout.model.WorkoutSet
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    private val database: DbWorkoutDatabase,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val recordDao: RecordDao,
) {
    fun observeExercises(): Flow<List<Exercise>> = exerciseDao.observeAll().map { entities ->
        entities.map(ExerciseEntity::toModel)
    }

    suspend fun getExercise(id: Long): Exercise? = exerciseDao.getById(id)?.toModel()

    suspend fun saveCustomExercise(
        id: Long?,
        name: String,
        category: ExerciseCategory,
        notes: String?,
    ): Long {
        val normalizedName = name.trim()
        return if (id == null) {
            exerciseDao.insert(
                ExerciseEntity(
                    name = normalizedName,
                    category = category,
                    isCustom = true,
                    notes = notes?.trim()?.takeIf(String::isNotEmpty),
                    createdAt = System.currentTimeMillis(),
                ),
            )
        } else {
            val current = requireNotNull(exerciseDao.getById(id))
            require(current.isCustom)
            exerciseDao.update(
                current.copy(
                    name = normalizedName,
                    category = category,
                    notes = notes?.trim()?.takeIf(String::isNotEmpty),
                ),
            )
            id
        }
    }

    suspend fun deleteCustomExercise(id: Long): Boolean = exerciseDao.deleteCustom(id) > 0

    fun observeRecordExercises(): Flow<List<RecordListItem>> =
        recordDao.observeAllWithExercise().map { records ->
            records
                .groupBy { it.record.exerciseId }
                .map { (_, items) ->
                    val latest = items.maxBy { it.record.dateEpochDay }
                    RecordListItem(
                        exercise = latest.exercise.toModel(),
                        weightKg = latest.record.weightKg,
                        date = LocalDate.ofEpochDay(latest.record.dateEpochDay),
                        notes = latest.record.notes,
                    )
                }
                .sortedByDescending { it.date }
        }

    fun observeRecordsForExercise(exerciseId: Long): Flow<List<ExerciseRecord>> =
        recordDao.observeByExercise(exerciseId).map { entities -> entities.map(RecordEntity::toModel) }

    suspend fun getRecord(id: Long): ExerciseRecord? = recordDao.getById(id)?.toModel()

    suspend fun saveRecord(
        id: Long?,
        exerciseId: Long,
        weightKg: Double,
        date: LocalDate,
        notes: String? = null,
    ): Long {
        val epochDay = date.toEpochDay()
        val normalizedNotes = notes?.trim()?.takeIf(String::isNotEmpty)
        val existing = recordDao.getByExerciseAndDate(exerciseId, epochDay)
        return when {
            id == null -> if (existing != null) {
                recordDao.update(existing.copy(weightKg = weightKg, notes = normalizedNotes))
                existing.id
            } else {
                recordDao.insert(
                    RecordEntity(
                        exerciseId = exerciseId,
                        weightKg = weightKg,
                        dateEpochDay = epochDay,
                        createdAt = System.currentTimeMillis(),
                        notes = normalizedNotes,
                    ),
                )
            }
            existing != null && existing.id != id -> throw DuplicateRecordDateException()
            else -> {
                val current = requireNotNull(recordDao.getById(id))
                recordDao.update(
                    current.copy(
                        exerciseId = exerciseId,
                        weightKg = weightKg,
                        dateEpochDay = epochDay,
                        notes = normalizedNotes,
                    ),
                )
                id
            }
        }
    }

    suspend fun deleteRecord(id: Long) = recordDao.deleteById(id)

    fun observeWorkouts(start: LocalDate, end: LocalDate): Flow<List<WorkoutListItem>> =
        workoutDao.observeSummaries(start.toEpochDay(), end.toEpochDay()).map { summaries ->
            summaries.map { summary ->
                WorkoutListItem(
                    id = summary.id,
                    date = LocalDate.ofEpochDay(summary.dateEpochDay),
                    exerciseCount = summary.exerciseCount,
                )
            }
        }

    fun observeMonth(month: YearMonth): Flow<List<WorkoutListItem>> = observeWorkouts(
        month.atDay(1),
        month.atEndOfMonth(),
    )

    fun observeWorkout(id: Long): Flow<Workout?> = workoutDao.observeById(id).map { it?.toModel() }

    suspend fun getWorkout(id: Long): Workout? = workoutDao.getById(id)?.toModel()

    suspend fun findWorkoutId(date: LocalDate): Long? =
        workoutDao.getEntityByDate(date.toEpochDay())?.id

    suspend fun saveWorkout(draft: WorkoutDraft): Long = database.withTransaction {
        val now = System.currentTimeMillis()
        val sameDateWorkout = workoutDao.getEntityByDate(draft.date.toEpochDay())
        val workoutId = when {
            draft.id == null && sameDateWorkout != null -> throw DuplicateWorkoutDateException()
            draft.id == null -> workoutDao.insertWorkout(
                WorkoutEntity(
                    dateEpochDay = draft.date.toEpochDay(),
                    notes = draft.notes?.trim()?.takeIf(String::isNotEmpty),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            else -> {
                if (sameDateWorkout != null && sameDateWorkout.id != draft.id) {
                    throw DuplicateWorkoutDateException()
                }
                val current = requireNotNull(workoutDao.getById(draft.id))
                workoutDao.updateWorkout(
                    current.workout.copy(
                        dateEpochDay = draft.date.toEpochDay(),
                        notes = draft.notes?.trim()?.takeIf(String::isNotEmpty),
                        updatedAt = now,
                    ),
                )
                draft.id
            }
        }

        workoutDao.deleteExercisesForWorkout(workoutId)
        draft.exercises.sortedBy { it.orderIndex }.forEachIndexed { index, exercise ->
            val workoutExerciseId = workoutDao.insertWorkoutExercise(
                WorkoutExerciseEntity(
                    workoutId = workoutId,
                    exerciseId = exercise.exerciseId,
                    orderIndex = index,
                ),
            )
            workoutDao.insertSets(
                exercise.sets.mapIndexed { setIndex, set ->
                    ExerciseSetEntity(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setIndex + 1,
                        reps = set.reps,
                        weightKg = set.weightKg,
                    )
                },
            )
        }
        workoutId
    }

    suspend fun deleteWorkout(id: Long) {
        val workout = workoutDao.getById(id)?.workout ?: return
        workoutDao.deleteWorkout(workout)
    }
}

class DuplicateWorkoutDateException : IllegalStateException()

class DuplicateRecordDateException : IllegalStateException()

private fun ExerciseEntity.toModel() = Exercise(id, name, category, isCustom, notes)

private fun RecordEntity.toModel() = ExerciseRecord(
    id = id,
    exerciseId = exerciseId,
    weightKg = weightKg,
    date = LocalDate.ofEpochDay(dateEpochDay),
    notes = notes,
)

private fun WorkoutWithExercises.toModel() = Workout(
    id = workout.id,
    date = LocalDate.ofEpochDay(workout.dateEpochDay),
    notes = workout.notes,
    createdAt = workout.createdAt,
    updatedAt = workout.updatedAt,
    exercises = exercises.sortedBy { it.workoutExercise.orderIndex }.map { relation ->
        WorkoutExercise(
            id = relation.workoutExercise.id,
            exercise = relation.exercise.toModel(),
            orderIndex = relation.workoutExercise.orderIndex,
            sets = relation.sets.sortedBy { it.setNumber }.map { set ->
                WorkoutSet(set.id, set.setNumber, set.reps, set.weightKg)
            },
        )
    },
)
