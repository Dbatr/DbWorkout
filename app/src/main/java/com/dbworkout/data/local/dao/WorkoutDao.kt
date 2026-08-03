package com.dbworkout.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dbworkout.data.local.entity.ExerciseSetEntity
import com.dbworkout.data.local.entity.WorkoutEntity
import com.dbworkout.data.local.entity.WorkoutExerciseEntity
import com.dbworkout.data.local.entity.WorkoutSummary
import com.dbworkout.data.local.entity.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query(
        """
        SELECT w.id, w.dateEpochDay, COUNT(we.id) AS exerciseCount
        FROM workouts w
        LEFT JOIN workout_exercises we ON we.workoutId = w.id
        WHERE w.dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY w.id
        ORDER BY w.dateEpochDay DESC
        """,
    )
    fun observeSummaries(startEpochDay: Long, endEpochDay: Long): Flow<List<WorkoutSummary>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): WorkoutWithExercises?

    @Query("SELECT * FROM workouts WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getEntityByDate(dateEpochDay: Long): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Insert
    suspend fun insertWorkoutExercise(exercise: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: Long)
}
