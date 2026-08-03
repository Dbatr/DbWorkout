package com.dbworkout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.dbworkout.ui.util.formatRussianDate
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class WorkoutFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createWorkout_andOpenSavedDetails() {
        composeRule.onNodeWithContentDescription("Добавить тренировку").performClick()

        addExercise(
            query = "подт",
            name = "Подтягивания прямым хватом",
            reps = listOf("10", "9", "8"),
        )
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Подтягивания прямым хватом").assertIsDisplayed()

        addExercise(
            query = "класс",
            name = "Классические отжимания",
            reps = listOf("20", "18", "15"),
        )

        composeRule.onNodeWithContentDescription("Сохранить").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeRule.onNodeWithText("Тренировка сохранена").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }

        composeRule.onNodeWithText(LocalDate.now().formatRussianDate()).performClick()
        composeRule.onNodeWithText("Подтягивания прямым хватом").assertIsDisplayed()
        composeRule.onNodeWithText("10 повторений").assertIsDisplayed()
        composeRule.onNodeWithText("Классические отжимания").assertIsDisplayed()

        composeRule.onNodeWithText("8 повторений").assertIsDisplayed()
        composeRule.onNodeWithText("15 повторений").assertIsDisplayed()
    }

    private fun addExercise(query: String, name: String, reps: List<String>) {
        composeRule.onNodeWithText("Добавить упражнение").performClick()
        composeRule.onNodeWithTag("exercise_search").performTextInput(query)
        composeRule.onNodeWithText(name).performClick()

        composeRule.onNodeWithTag("reps_0").performTextInput(reps.first())
        reps.drop(1).forEachIndexed { index, value ->
            composeRule.onNodeWithTag("add_set").performClick()
            composeRule.onNodeWithTag("reps_${index + 1}").performTextClearance()
            composeRule.onNodeWithTag("reps_${index + 1}").performTextInput(value)
        }
        composeRule.onNodeWithContentDescription("Сохранить").performClick()
    }
}
