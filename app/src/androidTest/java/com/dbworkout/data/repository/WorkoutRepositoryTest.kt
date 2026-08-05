package com.dbworkout.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dbworkout.data.local.database.DbWorkoutDatabase
import com.dbworkout.data.local.entity.ExerciseEntity
import com.dbworkout.model.ExerciseCategory
import com.dbworkout.model.WorkoutDraft
import com.dbworkout.model.WorkoutExerciseDraft
import com.dbworkout.model.WorkoutSetDraft
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutRepositoryTest {
    private lateinit var database: DbWorkoutDatabase
    private lateinit var repository: WorkoutRepository
    private var exerciseId: Long = 0

    @Before
    fun setUp() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DbWorkoutDatabase::class.java,
        ).build()
        repository = WorkoutRepository(database, database.exerciseDao(), database.workoutDao())
        exerciseId = database.exerciseDao().insert(
            ExerciseEntity(
                name = "Тестовое упражнение",
                category = ExerciseCategory.OTHER,
                isCustom = true,
                notes = null,
                createdAt = 1,
            ),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun savingNewWorkoutOnOccupiedDateFailsWithoutChangingExistingWorkout() = runBlocking {
        val date = LocalDate.of(2026, 8, 5)
        val originalId = repository.saveWorkout(draft(date, notes = "Исходная заметка", reps = 10))

        assertThrows(DuplicateWorkoutDateException::class.java) {
            runBlocking {
                repository.saveWorkout(draft(date, notes = "Новая заметка", reps = 20))
            }
        }

        val saved = requireNotNull(repository.getWorkout(originalId))
        assertEquals("Исходная заметка", saved.notes)
        assertEquals(10, saved.exercises.single().sets.single().reps)
    }

    @Test
    fun savingNewWorkoutOnFreeDateCreatesSeparateWorkout() = runBlocking {
        val today = LocalDate.of(2026, 8, 5)
        val yesterday = today.minusDays(1)
        val todayId = repository.saveWorkout(draft(today, notes = "Сегодня", reps = 10))

        val yesterdayId = repository.saveWorkout(draft(yesterday, notes = "Вчера", reps = 12))

        assertEquals(todayId, repository.findWorkoutId(today))
        assertEquals(yesterdayId, repository.findWorkoutId(yesterday))
        assertEquals("Сегодня", repository.getWorkout(todayId)?.notes)
        assertEquals("Вчера", repository.getWorkout(yesterdayId)?.notes)
    }

    private fun draft(date: LocalDate, notes: String, reps: Int) = WorkoutDraft(
        id = null,
        date = date,
        notes = notes,
        exercises = listOf(
            WorkoutExerciseDraft(
                exerciseId = exerciseId,
                orderIndex = 0,
                sets = listOf(WorkoutSetDraft(setNumber = 1, reps = reps, weightKg = null)),
            ),
        ),
    )
}
