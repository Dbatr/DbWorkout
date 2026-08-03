package com.dbworkout.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dbworkout.data.repository.AppThemeMode

private val LightColors = lightColorScheme(
    primary = Purple40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF21005D),
    background = LightBackground,
    surface = LightBackground,
)

private val DarkColors = darkColorScheme(
    primary = Purple80,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF4A2A9E),
    background = DarkBackground,
    surface = DarkBackground,
)

@Composable
fun DbWorkoutTheme(
    themeMode: AppThemeMode,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) DarkColors else LightColors

    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
