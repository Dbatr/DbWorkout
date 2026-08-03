package com.dbworkout.data.local.database

import androidx.room.TypeConverter
import com.dbworkout.model.ExerciseCategory

class DbConverters {
    @TypeConverter
    fun fromCategory(category: ExerciseCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): ExerciseCategory = ExerciseCategory.valueOf(value)
}
