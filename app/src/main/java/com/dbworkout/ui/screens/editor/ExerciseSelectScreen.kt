package com.dbworkout.ui.screens.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.model.Exercise
import com.dbworkout.model.ExerciseCategory
import com.dbworkout.ui.components.EmptyState
import com.dbworkout.ui.components.LoadingPane
import com.dbworkout.ui.util.labelRes
import com.dbworkout.viewmodel.ExerciseListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectScreen(
    viewModel: ExerciseListViewModel,
    onBack: () -> Unit,
    onSelect: (Exercise) -> Unit,
    onCreateCustom: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.choose_exercise), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCustom) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_custom_exercise))
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).testTag("exercise_search"),
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
                            supportingContent = { Text(stringResource(exercise.category.labelRes())) },
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(exercise) },
                            trailingContent = {
                                if (exercise.isCustom) Text(
                                    stringResource(R.string.custom_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                            leadingContent = { Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.primary) },
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
