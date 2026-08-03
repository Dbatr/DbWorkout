package com.dbworkout.ui.util

import androidx.annotation.StringRes
import com.dbworkout.R
import com.dbworkout.model.ExerciseCategory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruLocale = Locale.forLanguageTag("ru-RU")
private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", ruLocale)
private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", ruLocale)
private val weightFormatter = DecimalFormat("0.##", DecimalFormatSymbols(ruLocale))

fun LocalDate.formatRussianDate(): String = format(fullDateFormatter)
fun YearMonth.formatRussianMonth(): String = format(monthFormatter).replaceFirstChar { it.titlecase(ruLocale) }
fun Double.formatWeight(): String = weightFormatter.format(this)

@StringRes
fun ExerciseCategory.labelRes(): Int = when (this) {
    ExerciseCategory.BACK -> R.string.category_back
    ExerciseCategory.CHEST -> R.string.category_chest
    ExerciseCategory.LEGS -> R.string.category_legs
    ExerciseCategory.SHOULDERS -> R.string.category_shoulders
    ExerciseCategory.BICEPS -> R.string.category_biceps
    ExerciseCategory.TRICEPS -> R.string.category_triceps
    ExerciseCategory.ABS -> R.string.category_abs
    ExerciseCategory.CARDIO -> R.string.category_cardio
    ExerciseCategory.OTHER -> R.string.category_other
}
