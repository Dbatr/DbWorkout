package com.dbworkout.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.ui.components.DatePickerModal
import com.dbworkout.ui.components.DrawerScaffold
import com.dbworkout.ui.components.EmptyState
import com.dbworkout.ui.components.LoadingPane
import com.dbworkout.ui.components.WorkoutCard
import com.dbworkout.ui.navigation.Routes
import com.dbworkout.ui.util.formatRussianMonth
import com.dbworkout.viewmodel.CalendarViewModel

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigate: (String) -> Unit,
    onCreateWorkout: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        DatePickerModal(
            initialDate = state.selectedDate ?: state.month.atDay(1),
            onDismiss = { showPicker = false },
            onDateSelected = viewModel::selectDate,
        )
    }
    DrawerScaffold(
        title = stringResource(R.string.nav_calendar),
        currentRoute = Routes.CALENDAR,
        onNavigate = onNavigate,
        onCreateWorkout = onCreateWorkout,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBackIos, contentDescription = stringResource(R.string.previous))
                }
                Text(
                    state.month.formatRussianMonth(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = stringResource(R.string.next))
                }
            }
            Button(onClick = { showPicker = true }, modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.Rounded.CalendarMonth, null)
                Text(stringResource(R.string.select_date), Modifier.padding(start = 8.dp))
            }
            Text(
                stringResource(R.string.calendar_pick_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            when {
                state.isLoading -> LoadingPane()
                state.workouts.isEmpty() -> EmptyState(
                    title = stringResource(R.string.calendar_month_empty),
                    actionLabel = stringResource(R.string.create_workout),
                    onAction = onCreateWorkout,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.workouts, key = { it.id }) { workout ->
                        WorkoutCard(
                            item = workout,
                            selected = workout.date == state.selectedDate,
                            onClick = { onOpenWorkout(workout.id) },
                        )
                    }
                }
            }
        }
    }
}
