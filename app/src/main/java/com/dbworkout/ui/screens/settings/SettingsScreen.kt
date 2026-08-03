package com.dbworkout.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.R
import com.dbworkout.data.repository.AppThemeMode
import com.dbworkout.ui.components.DrawerScaffold
import com.dbworkout.ui.navigation.Routes
import com.dbworkout.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (String) -> Unit,
    onCreateWorkout: () -> Unit,
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    DrawerScaffold(
        title = stringResource(R.string.nav_settings),
        currentRoute = Routes.SETTINGS,
        onNavigate = onNavigate,
        onCreateWorkout = onCreateWorkout,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(stringResource(R.string.app_theme), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                AppThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
                        AppThemeMode.DARK -> stringResource(R.string.theme_dark)
                    }
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.setThemeMode(mode) }.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = themeMode == mode, onClick = { viewModel.setThemeMode(mode) })
                        Text(label, Modifier.padding(start = 8.dp))
                    }
                }
            }
            Text(stringResource(R.string.weight_units), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Text(stringResource(R.string.kilograms), Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.units_future_hint),
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
