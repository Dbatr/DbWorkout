package com.dbworkout.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import com.dbworkout.model.ExerciseCategory

internal object SeedExercises {
    private val exercises = listOf(
        ExerciseCategory.BACK to "Подтягивания прямым хватом",
        ExerciseCategory.BACK to "Подтягивания обратным хватом",
        ExerciseCategory.BACK to "Подтягивания широким хватом",
        ExerciseCategory.BACK to "Подтягивания узким хватом",
        ExerciseCategory.BACK to "Негативные подтягивания",
        ExerciseCategory.BACK to "Австралийские подтягивания",
        ExerciseCategory.CHEST to "Классические отжимания",
        ExerciseCategory.CHEST to "Отжимания широким хватом",
        ExerciseCategory.CHEST to "Отжимания узким хватом",
        ExerciseCategory.CHEST to "Алмазные отжимания",
        ExerciseCategory.CHEST to "Отжимания с ногами на возвышенности",
        ExerciseCategory.CHEST to "Отжимания с руками на возвышенности",
        ExerciseCategory.CHEST to "Взрывные отжимания",
        ExerciseCategory.CHEST to "Отжимания на брусьях",
        ExerciseCategory.CHEST to "Отжимания на брусьях с дополнительным весом",
        ExerciseCategory.LEGS to "Приседания",
        ExerciseCategory.LEGS to "Приседания с гантелями",
        ExerciseCategory.LEGS to "Выпады",
        ExerciseCategory.LEGS to "Обратные выпады",
        ExerciseCategory.LEGS to "Болгарские выпады",
        ExerciseCategory.LEGS to "Приседания на одной ноге",
        ExerciseCategory.LEGS to "Подъёмы на носки",
        ExerciseCategory.LEGS to "Ягодичный мост",
        ExerciseCategory.ABS to "Скручивания",
        ExerciseCategory.ABS to "Подъём ног лёжа",
        ExerciseCategory.ABS to "Подъём ног в висе",
        ExerciseCategory.ABS to "Подъём коленей в висе",
        ExerciseCategory.ABS to "Планка",
        ExerciseCategory.ABS to "Боковая планка",
        ExerciseCategory.ABS to "Велосипед",
        ExerciseCategory.ABS to "Русские скручивания",
        ExerciseCategory.SHOULDERS to "Отжимания уголком (Pike Push-ups)",
        ExerciseCategory.SHOULDERS to "Стойка у стены",
        ExerciseCategory.SHOULDERS to "Отжимания в стойке у стены",
        ExerciseCategory.BICEPS to "Сгибания рук с гантелями на бицепс",
        ExerciseCategory.BICEPS to "Молотковые сгибания",
        ExerciseCategory.TRICEPS to "Разгибание гантели из-за головы на трицепс",
        ExerciseCategory.CHEST to "Жим штанги лёжа",
        ExerciseCategory.LEGS to "Приседания со штангой",
        ExerciseCategory.BACK to "Тяга штанги в наклоне",
        ExerciseCategory.SHOULDERS to "Жим гантелей сидя",
    )

    fun populate(database: SupportSQLiteDatabase) {
        val createdAt = System.currentTimeMillis()
        exercises.forEach { (category, name) ->
            database.execSQL(
                "INSERT INTO exercises (name, category, isCustom, notes, createdAt) VALUES (?, ?, 0, NULL, ?)",
                arrayOf<Any>(name, category.name, createdAt),
            )
        }
    }
}
