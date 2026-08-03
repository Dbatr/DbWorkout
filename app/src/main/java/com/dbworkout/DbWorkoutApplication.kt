package com.dbworkout

import android.app.Application
import com.dbworkout.data.local.database.DbWorkoutDatabase
import com.dbworkout.data.repository.SettingsRepository
import com.dbworkout.data.repository.WorkoutRepository

class DbWorkoutApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(application: Application) {
    private val database = DbWorkoutDatabase.getInstance(application)
    val workoutRepository = WorkoutRepository(database, database.exerciseDao(), database.workoutDao())
    val settingsRepository = SettingsRepository(application)
}
