package com.dbworkout.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dbworkout.R
import com.dbworkout.ui.navigation.Routes
import kotlinx.coroutines.launch

private data class DrawerItem(val route: String, val labelRes: Int, val icon: ImageVector)

private val drawerItems = listOf(
    DrawerItem(Routes.HOME, R.string.nav_home, Icons.Rounded.Home),
    DrawerItem(Routes.CALENDAR, R.string.nav_calendar, Icons.Rounded.CalendarMonth),
    DrawerItem("create", R.string.nav_create_workout, Icons.Rounded.Add),
    DrawerItem(Routes.EXERCISES, R.string.nav_exercises, Icons.Rounded.FitnessCenter),
    DrawerItem(Routes.SETTINGS, R.string.nav_settings, Icons.Rounded.Settings),
    DrawerItem(Routes.ABOUT, R.string.nav_about, Icons.Rounded.Info),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScaffold(
    title: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateWorkout: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 16.dp),
                    ) {
                        Icon(
                            Icons.Rounded.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(13.dp).size(30.dp),
                        )
                    }
                    Text(
                        stringResource(R.string.my_workouts),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(stringResource(item.labelRes)) },
                            selected = item.route == currentRoute,
                            icon = { Icon(item.icon, contentDescription = null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (item.route == "create") onCreateWorkout() else onNavigate(item.route)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = stringResource(R.string.menu))
                        }
                    },
                    actions = { actions() },
                )
            },
            floatingActionButton = { floatingActionButton() },
            content = content,
        )
    }
}

@Composable
fun AddWorkoutFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_workout))
    }
}
