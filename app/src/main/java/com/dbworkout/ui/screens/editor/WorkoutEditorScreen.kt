package com.dbworkout.ui.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.ui.components.DatePickerModal
import com.dbworkout.ui.components.LoadingPane
import com.dbworkout.ui.util.formatRussianDate
import com.dbworkout.viewmodel.WorkoutEditorError
import com.dbworkout.viewmodel.WorkoutEditorEvent
import com.dbworkout.viewmodel.WorkoutEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditorScreen(
    viewModel: WorkoutEditorViewModel,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onEditSets: (Long) -> Unit,
    onSaved: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    val errorMessage = when (state.error) {
        WorkoutEditorError.NO_EXERCISES -> stringResource(R.string.error_no_exercises)
        WorkoutEditorError.INVALID_SETS -> stringResource(R.string.error_invalid_sets)
        WorkoutEditorError.DUPLICATE_DATE -> stringResource(R.string.error_duplicate_date)
        WorkoutEditorError.SAVE_FAILED -> stringResource(R.string.error_save_workout)
        null -> null
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is WorkoutEditorEvent.Saved) onSaved(event.workoutId)
        }
    }
    if (showDatePicker) {
        DatePickerModal(
            initialDate = state.date,
            onDismiss = { showDatePicker = false },
            onDateSelected = viewModel::setDate,
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (state.workoutId == null) R.string.new_workout else R.string.edit_workout),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::save, enabled = !state.isSaving && !state.isLoading) {
                        Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.save))
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingPane(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.isSaving) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                item { Text(stringResource(R.string.date), style = MaterialTheme.typography.labelLarge) }
                item {
                    OutlinedCard(onClick = { showDatePicker = true }, shape = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(state.date.formatRussianDate(), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = stringResource(R.string.date_field_description))
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.workout_exercises),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                if (state.exercises.isEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Text(
                                stringResource(R.string.no_exercises_in_workout),
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                itemsIndexed(state.exercises, key = { _, item -> item.key }) { index, exercise ->
                    Card(
                        onClick = { onEditSets(exercise.key) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.DragHandle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(exercise.name, fontWeight = FontWeight.Medium)
                                Text(
                                    pluralStringResource(R.plurals.set_count, exercise.sets.size, exercise.sets.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column {
                                IconButton(
                                    onClick = { viewModel.moveExercise(exercise.key, -1) },
                                    enabled = index > 0,
                                ) { Icon(Icons.Rounded.ArrowUpward, stringResource(R.string.move_up)) }
                                IconButton(
                                    onClick = { viewModel.moveExercise(exercise.key, 1) },
                                    enabled = index < state.exercises.lastIndex,
                                ) { Icon(Icons.Rounded.ArrowDownward, stringResource(R.string.move_down)) }
                            }
                            IconButton(onClick = { viewModel.removeExercise(exercise.key) }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.remove_exercise))
                            }
                        }
                    }
                }
                item {
                    Button(onClick = onAddExercise, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Add, null)
                        Text(stringResource(R.string.add_exercise), modifier = Modifier.padding(start = 8.dp))
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::setNotes,
                        label = { Text(stringResource(R.string.workout_notes)) },
                        placeholder = { Text(stringResource(R.string.workout_notes_hint)) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}
