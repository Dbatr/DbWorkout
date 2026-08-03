package com.dbworkout.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dbworkout.data.local.dao.ExerciseDao
import com.dbworkout.data.local.dao.WorkoutDao
import com.dbworkout.data.local.entity.ExerciseEntity
import com.dbworkout.data.local.entity.ExerciseSetEntity
import com.dbworkout.data.local.entity.WorkoutEntity
import com.dbworkout.data.local.entity.WorkoutExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseSetEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DbConverters::class)
abstract class DbWorkoutDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var instance: DbWorkoutDatabase? = null

        fun getInstance(context: Context): DbWorkoutDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DbWorkoutDatabase::class.java,
                "dbworkout.db",
            ).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    SeedExercises.populate(db)
                }
            }).build().also { instance = it }
        }
    }
}
