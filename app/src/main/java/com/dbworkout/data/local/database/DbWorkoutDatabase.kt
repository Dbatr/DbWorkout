package com.dbworkout.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dbworkout.data.local.dao.ExerciseDao
import com.dbworkout.data.local.dao.RecordDao
import com.dbworkout.data.local.dao.WorkoutDao
import com.dbworkout.data.local.entity.ExerciseEntity
import com.dbworkout.data.local.entity.ExerciseSetEntity
import com.dbworkout.data.local.entity.RecordEntity
import com.dbworkout.data.local.entity.WorkoutEntity
import com.dbworkout.data.local.entity.WorkoutExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        ExerciseSetEntity::class,
        RecordEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(DbConverters::class)
abstract class DbWorkoutDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var instance: DbWorkoutDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `records` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`exerciseId` INTEGER NOT NULL, " +
                        "`weightKg` REAL NOT NULL, " +
                        "`dateEpochDay` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_records_exerciseId` ON `records` (`exerciseId`)")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_records_exerciseId_dateEpochDay` " +
                        "ON `records` (`exerciseId`, `dateEpochDay`)",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `records` ADD COLUMN `notes` TEXT")
            }
        }

        fun getInstance(context: Context): DbWorkoutDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DbWorkoutDatabase::class.java,
                "dbworkout.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    SeedExercises.populate(db)
                }
            }).build().also { instance = it }
        }
    }
}
