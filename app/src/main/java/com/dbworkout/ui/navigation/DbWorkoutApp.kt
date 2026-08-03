package com.dbworkout.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dbworkout.AppContainer
import com.dbworkout.R
import com.dbworkout.ui.screens.about.AboutScreen
import com.dbworkout.ui.screens.calendar.CalendarScreen
import com.dbworkout.ui.screens.editor.ExerciseSelectScreen
import com.dbworkout.ui.screens.editor.ExerciseSetsScreen
import com.dbworkout.ui.screens.editor.WorkoutEditorScreen
import com.dbworkout.ui.screens.exercises.CustomExerciseScreen
import com.dbworkout.ui.screens.exercises.ExerciseLibraryScreen
import com.dbworkout.ui.screens.home.HomeScreen
import com.dbworkout.ui.screens.settings.SettingsScreen
import com.dbworkout.ui.screens.workout.WorkoutDetailScreen
import com.dbworkout.viewmodel.CalendarViewModel
import com.dbworkout.viewmodel.CustomExerciseViewModel
import com.dbworkout.viewmodel.ExerciseListViewModel
import com.dbworkout.viewmodel.HomeViewModel
import com.dbworkout.viewmodel.SettingsViewModel
import com.dbworkout.viewmodel.WorkoutDetailViewModel
import com.dbworkout.viewmodel.WorkoutEditorViewModel
import com.dbworkout.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DbWorkoutApp(
    container: AppContainer,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.workout_saved)
    val deletedMessage = stringResource(R.string.workout_deleted)
    val exerciseSavedMessage = stringResource(R.string.exercise_saved)
    val editorViewModel: WorkoutEditorViewModel = viewModel(
        key = "workout-editor",
        factory = viewModelFactory { WorkoutEditorViewModel(container.workoutRepository) },
    )
    val selectionViewModel: ExerciseListViewModel = viewModel(
        key = "exercise-selection",
        factory = viewModelFactory { ExerciseListViewModel(container.workoutRepository) },
    )
    var editorStage by rememberSaveable { mutableStateOf(EditorStage.CLOSED) }
    var activeDraftKey by rememberSaveable { mutableLongStateOf(-1L) }
    var resumeSelectionAfterCustom by rememberSaveable { mutableStateOf(false) }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun returnHome() {
        navController.navigate(Routes.HOME) {
            popUpTo(navController.graph.id) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun openTodayEditor() {
        scope.launch {
            val existingId = container.workoutRepository.findWorkoutId(LocalDate.now())
            withContext(Dispatchers.Main.immediate) {
                navigateTopLevel(Routes.HOME)
                editorViewModel.start(existingId)
                editorStage = EditorStage.EDIT
            }
        }
    }

    BackHandler(enabled = editorStage != EditorStage.CLOSED) {
        editorStage = when (editorStage) {
            EditorStage.SELECT, EditorStage.SETS -> EditorStage.EDIT
            EditorStage.EDIT -> EditorStage.CLOSED
            EditorStage.CLOSED -> EditorStage.CLOSED
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel(factory = viewModelFactory {
                    HomeViewModel(container.workoutRepository)
                })
                HomeScreen(
                    viewModel = vm,
                    onNavigate = ::navigateTopLevel,
                    onCreateWorkout = ::openTodayEditor,
                    onOpenWorkout = { navController.navigate(Routes.workout(it)) },
                )
            }
            composable(
                route = Routes.WORKOUT,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
            ) { entry ->
                val workoutId = requireNotNull(entry.arguments?.getLong("workoutId"))
                val vm: WorkoutDetailViewModel = viewModel(
                    factory = viewModelFactory { WorkoutDetailViewModel(workoutId, container.workoutRepository) },
                )
                WorkoutDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        editorViewModel.start(workoutId)
                        editorStage = EditorStage.EDIT
                    },
                    onDeleted = {
                        returnHome()
                        scope.launch { snackbarHostState.showSnackbar(deletedMessage) }
                    },
                )
            }

            composable(Routes.EXERCISES) {
                val vm: ExerciseListViewModel = viewModel(factory = viewModelFactory {
                    ExerciseListViewModel(container.workoutRepository)
                })
                ExerciseLibraryScreen(
                    viewModel = vm,
                    snackbarHostState = snackbarHostState,
                    onNavigate = ::navigateTopLevel,
                    onCreateWorkout = ::openTodayEditor,
                    onCreateExercise = { navController.navigate(Routes.customExercise()) },
                    onEditExercise = { navController.navigate(Routes.customExercise(it)) },
                )
            }
            composable(
                route = Routes.CUSTOM_EXERCISE,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType; defaultValue = -1L }),
            ) { entry ->
                val id = entry.arguments?.getLong("exerciseId")?.takeIf { it >= 0 }
                val vm: CustomExerciseViewModel = viewModel(factory = viewModelFactory {
                    CustomExerciseViewModel(id, container.workoutRepository)
                })
                CustomExerciseScreen(
                    viewModel = vm,
                    onBack = {
                        navController.popBackStack()
                        if (resumeSelectionAfterCustom) {
                            resumeSelectionAfterCustom = false
                            editorStage = EditorStage.SELECT
                        }
                    },
                    onSaved = {
                        navController.popBackStack()
                        if (resumeSelectionAfterCustom) {
                            resumeSelectionAfterCustom = false
                            editorStage = EditorStage.SELECT
                        }
                        scope.launch { snackbarHostState.showSnackbar(exerciseSavedMessage) }
                    },
                )
            }
            composable(Routes.CALENDAR) {
                val vm: CalendarViewModel = viewModel(factory = viewModelFactory {
                    CalendarViewModel(container.workoutRepository)
                })
                CalendarScreen(
                    viewModel = vm,
                    onNavigate = ::navigateTopLevel,
                    onCreateWorkout = ::openTodayEditor,
                    onOpenWorkout = { navController.navigate(Routes.workout(it)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigate = ::navigateTopLevel,
                    onCreateWorkout = ::openTodayEditor,
                )
            }
            composable(Routes.ABOUT) {
                AboutScreen(onNavigate = ::navigateTopLevel, onCreateWorkout = ::openTodayEditor)
            }
        }
        when (editorStage) {
            EditorStage.CLOSED -> Unit
            EditorStage.EDIT -> WorkoutEditorScreen(
                viewModel = editorViewModel,
                onBack = { editorStage = EditorStage.CLOSED },
                onAddExercise = {
                    selectionViewModel.resetFilters()
                    editorStage = EditorStage.SELECT
                },
                onEditSets = { key ->
                    activeDraftKey = key
                    editorStage = EditorStage.SETS
                },
                onSaved = {
                    editorStage = EditorStage.CLOSED
                    scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                },
            )
            EditorStage.SELECT -> ExerciseSelectScreen(
                viewModel = selectionViewModel,
                onBack = { editorStage = EditorStage.EDIT },
                onSelect = { exercise ->
                    activeDraftKey = editorViewModel.addExercise(exercise)
                    editorStage = EditorStage.SETS
                },
                onCreateCustom = {
                    resumeSelectionAfterCustom = true
                    editorStage = EditorStage.CLOSED
                    navController.navigate(Routes.customExercise())
                },
            )
            EditorStage.SETS -> ExerciseSetsScreen(
                draftKey = activeDraftKey,
                viewModel = editorViewModel,
                onBack = { editorStage = EditorStage.EDIT },
                onDone = { editorStage = EditorStage.EDIT },
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
        )
    }
}

private enum class EditorStage { CLOSED, EDIT, SELECT, SETS }
