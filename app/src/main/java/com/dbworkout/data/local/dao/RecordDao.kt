package com.dbworkout.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dbworkout.data.local.entity.RecordEntity
import com.dbworkout.data.local.entity.RecordWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Transaction
    @Query("SELECT * FROM records")
    fun observeAllWithExercise(): Flow<List<RecordWithExercise>>

    @Query("SELECT * FROM records WHERE exerciseId = :exerciseId ORDER BY dateEpochDay DESC")
    fun observeByExercise(exerciseId: Long): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): RecordEntity?

    @Query("SELECT * FROM records WHERE exerciseId = :exerciseId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getByExerciseAndDate(exerciseId: Long, dateEpochDay: Long): RecordEntity?

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
