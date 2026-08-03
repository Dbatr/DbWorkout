package com.dbworkout.ui.navigation

object Routes {
    const val HOME = "home"
    const val WORKOUT = "workout/{workoutId}"
    const val EDIT_WORKOUT = "workout/editor"
    const val SELECT_EXERCISE = "workout/editor/exercise/select"
    const val EDIT_SETS = "workout/editor/exercise/sets/{draftKey}"
    const val EXERCISES = "exercises"
    const val CUSTOM_EXERCISE = "exercise/custom?exerciseId={exerciseId}"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun workout(id: Long) = "workout/$id"
    fun sets(draftKey: Long) = "workout/editor/exercise/sets/$draftKey"
    fun customExercise(id: Long? = null) = "exercise/custom?exerciseId=${id ?: -1L}"
}
