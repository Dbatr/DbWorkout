package com.dbworkout.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.ui.components.AddWorkoutFab
import com.dbworkout.ui.components.DrawerScaffold
import com.dbworkout.ui.components.EmptyState
import com.dbworkout.ui.components.LoadingPane
import com.dbworkout.ui.components.WorkoutCard
import com.dbworkout.ui.navigation.Routes
import com.dbworkout.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    onCreateWorkout: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DrawerScaffold(
        title = stringResource(R.string.my_workouts),
        currentRoute = Routes.HOME,
        onNavigate = onNavigate,
        onCreateWorkout = onCreateWorkout,
        floatingActionButton = { AddWorkoutFab(onCreateWorkout) },
    ) { padding ->
        when {
            state.isLoading -> LoadingPane(Modifier.padding(padding))
            state.workouts.isEmpty() -> EmptyState(
                title = stringResource(R.string.empty_week_title),
                body = stringResource(R.string.empty_week_body),
                actionLabel = stringResource(R.string.create_workout),
                onAction = onCreateWorkout,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.workouts, key = { it.id }) { workout ->
                    WorkoutCard(item = workout, onClick = { onOpenWorkout(workout.id) })
                }
            }
        }
    }
}
