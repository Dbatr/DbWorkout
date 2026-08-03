package com.dbworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbworkout.data.repository.WorkoutRepository
import com.dbworkout.model.WorkoutListItem
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val workouts: List<WorkoutListItem> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(repository: WorkoutRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow<LocalDate?>(null)
    private val workouts = month.flatMapLatest(repository::observeMonth)

    val uiState: StateFlow<CalendarUiState> = combine(month, selectedDate, workouts) { currentMonth, date, items ->
        CalendarUiState(currentMonth, date, items, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun previousMonth() { month.value = month.value.minusMonths(1); selectedDate.value = null }
    fun nextMonth() { month.value = month.value.plusMonths(1); selectedDate.value = null }
    fun selectDate(date: LocalDate) { month.value = YearMonth.from(date); selectedDate.value = date }
}
