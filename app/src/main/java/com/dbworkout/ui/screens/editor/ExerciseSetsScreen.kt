package com.dbworkout.ui.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.ui.components.EmptyState
import com.dbworkout.viewmodel.WorkoutEditorError
import com.dbworkout.viewmodel.WorkoutEditorViewModel
import com.dbworkout.viewmodel.isValid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSetsScreen(
    draftKey: Long,
    viewModel: WorkoutEditorViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exercise = state.exercises.firstOrNull { it.key == draftKey }
    val snackbarHostState = remember { SnackbarHostState() }
    val errorText = if (state.error == WorkoutEditorError.INVALID_SETS) stringResource(R.string.error_invalid_sets) else null
    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.clearError()
        }
    }
    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configure_exercise), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { if (viewModel.exerciseHasValidSets(draftKey)) onDone() }) {
                        Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.save))
                    }
                },
            )
        },
    ) { padding ->
        if (exercise == null) {
            EmptyState(stringResource(R.string.no_exercises_in_workout), modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(exercise.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.set_input_tip),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.set_number), Modifier.weight(0.6f), style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.repetitions), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.weight_kg), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                        androidx.compose.foundation.layout.Spacer(Modifier.weight(0.45f))
                    }
                }
                itemsIndexed(exercise.sets, key = { _, set -> set.key }) { index, set ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text((index + 1).toString(), Modifier.weight(0.6f), style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = set.reps,
                            onValueChange = { value ->
                                if (value.all(Char::isDigit)) viewModel.updateSet(exercise.key, set.key, reps = value)
                            },
                            isError = set.reps.isNotBlank() && (set.reps.toIntOrNull() ?: 0) <= 0,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("reps_$index"),
                        )
                        OutlinedTextField(
                            value = set.weightKg,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() || it == '.' || it == ',' }) {
                                    viewModel.updateSet(exercise.key, set.key, weight = value)
                                }
                            },
                            isError = set.weightKg.isNotBlank() && !set.isValid(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("weight_$index"),
                        )
                        IconButton(
                            onClick = { viewModel.removeSet(exercise.key, set.key) },
                            enabled = exercise.sets.size > 1,
                            modifier = Modifier.weight(0.45f),
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.remove_set))
                        }
                    }
                }
                item {
                    Button(
                        onClick = { viewModel.addSet(exercise.key) },
                        modifier = Modifier.fillMaxWidth().testTag("add_set"),
                    ) {
                        Icon(Icons.Rounded.Add, null)
                        Text(stringResource(R.string.add_set), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
