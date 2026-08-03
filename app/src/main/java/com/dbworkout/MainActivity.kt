package com.dbworkout

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dbworkout.ui.navigation.DbWorkoutApp
import com.dbworkout.ui.theme.DbWorkoutTheme
import com.dbworkout.viewmodel.SettingsViewModel
import com.dbworkout.viewmodel.viewModelFactory
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels {
        viewModelFactory { SettingsViewModel((application as DbWorkoutApplication).container.settingsRepository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            DbWorkoutTheme(themeMode = themeMode) {
                DbWorkoutApp(
                    container = (application as DbWorkoutApplication).container,
                    settingsViewModel = settingsViewModel,
                )
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration).apply {
            setLocale(Locale.forLanguageTag("ru-RU"))
        }
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }
}
