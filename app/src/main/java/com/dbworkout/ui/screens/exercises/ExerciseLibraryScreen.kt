package com.dbworkout.ui.screens.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.model.Exercise
import com.dbworkout.model.ExerciseCategory
import com.dbworkout.ui.components.DrawerScaffold
import com.dbworkout.ui.components.EmptyState
import com.dbworkout.ui.components.LoadingPane
import com.dbworkout.ui.navigation.Routes
import com.dbworkout.ui.util.labelRes
import com.dbworkout.viewmodel.ExerciseListViewModel
import kotlinx.coroutines.launch

@Composable
fun ExerciseLibraryScreen(
    viewModel: ExerciseListViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    onCreateWorkout: () -> Unit,
    onCreateExercise: () -> Unit,
    onEditExercise: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    val deletedMessage = stringResource(R.string.exercise_deleted)
    val deleteFailedMessage = stringResource(R.string.exercise_delete_failed)

    exerciseToDelete?.let { exercise ->
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text(stringResource(R.string.delete_exercise_title)) },
            text = { Text(stringResource(R.string.delete_exercise_body)) },
            confirmButton = {
                TextButton(onClick = {
                    exerciseToDelete = null
                    scope.launch {
                        val result = viewModel.deleteCustom(exercise.id)
                        snackbarHostState.showSnackbar(if (result.isSuccess) deletedMessage else deleteFailedMessage)
                    }
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { exerciseToDelete = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    DrawerScaffold(
        title = stringResource(R.string.nav_exercises),
        currentRoute = Routes.EXERCISES,
        onNavigate = onNavigate,
        onCreateWorkout = onCreateWorkout,
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateExercise) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_custom_exercise))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_exercises)) },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    { IconButton(onClick = { viewModel.setQuery("") }) { Icon(Icons.Rounded.Clear, stringResource(R.string.clear_search)) } }
                } else null,
                singleLine = true,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    AssistChip(
                        onClick = { viewModel.setCategory(null) },
                        label = { Text(stringResource(R.string.filter_all)) },
                        leadingIcon = if (state.category == null) ({ Text("✓", color = MaterialTheme.colorScheme.primary) }) else null,
                    )
                }
                items(ExerciseCategory.entries) { category ->
                    AssistChip(
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(stringResource(category.labelRes())) },
                        leadingIcon = if (state.category == category) ({ Text("✓", color = MaterialTheme.colorScheme.primary) }) else null,
                    )
                }
            }
            when {
                state.isLoading -> LoadingPane()
                state.exercises.isEmpty() -> EmptyState(
                    title = stringResource(R.string.no_exercises_found),
                    body = stringResource(R.string.no_exercises_found_body),
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(state.exercises, key = { it.id }) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = {
                                Text(
                                    if (exercise.isCustom) {
                                        stringResource(R.string.custom_badge) + " · " + stringResource(exercise.category.labelRes())
                                    } else stringResource(exercise.category.labelRes()),
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = if (exercise.isCustom) {
                                {
                                    androidx.compose.foundation.layout.Row {
                                        IconButton(onClick = { onEditExercise(exercise.id) }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit))
                                        }
                                        IconButton(onClick = { exerciseToDelete = exercise }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete))
                                        }
                                    }
                                }
                            } else null,
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
