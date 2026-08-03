package com.dbworkout.ui.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dbworkout.BuildConfig
import com.dbworkout.R
import com.dbworkout.ui.components.DrawerScaffold
import com.dbworkout.ui.navigation.Routes

@Composable
fun AboutScreen(
    onNavigate: (String) -> Unit,
    onCreateWorkout: () -> Unit,
) {
    DrawerScaffold(
        title = stringResource(R.string.nav_about),
        currentRoute = Routes.ABOUT,
        onNavigate = onNavigate,
        onCreateWorkout = onCreateWorkout,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.size(24.dp))
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(24.dp)) {
                Icon(
                    Icons.Rounded.FitnessCenter,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(20.dp).size(42.dp),
                )
            }
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(stringResource(R.string.about_description), modifier = Modifier.padding(top = 8.dp))
            Text(
                stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            Card(
                modifier = Modifier.padding(top = 28.dp).widthIn(max = 420.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Text(
                    stringResource(R.string.offline_note),
                    modifier = Modifier.padding(18.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
