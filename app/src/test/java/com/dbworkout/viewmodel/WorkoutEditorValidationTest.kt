package com.dbworkout.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEditorValidationTest {
    @Test
    fun `positive repetitions without weight are valid`() {
        assertTrue(DraftSetUi(key = 1, reps = "10").isValid())
    }

    @Test
    fun `decimal weight with comma is valid`() {
        assertTrue(DraftSetUi(key = 1, reps = "8", weightKg = "7,5").isValid())
    }

    @Test
    fun `zero or missing repetitions are invalid`() {
        assertFalse(DraftSetUi(key = 1, reps = "0").isValid())
        assertFalse(DraftSetUi(key = 1, reps = "").isValid())
    }

    @Test
    fun `negative or malformed weight is invalid`() {
        assertFalse(DraftSetUi(key = 1, reps = "10", weightKg = "-1").isValid())
        assertFalse(DraftSetUi(key = 1, reps = "10", weightKg = "7..5").isValid())
    }
}
