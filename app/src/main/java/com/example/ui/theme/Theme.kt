package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SafaDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = AmoledBackground,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = TextEmerald,
    secondary = IslamicGold,
    onSecondary = AmoledBackground,
    secondaryContainer = IslamicGoldDark,
    onSecondaryContainer = TextGold,
    tertiary = SoftTeal,
    onTertiary = AmoledBackground,
    background = AmoledBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceHighlight,
    outlineVariant = GlassBorder,
    error = StatusNotAllowedRed,
    onError = Color.White,
    errorContainer = StatusNotAllowedRedContainer,
    onErrorContainer = Color(0xFFFECACA)
)

@Composable
fun SafaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = SafaDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AmoledBackground.toArgb()
            window.navigationBarColor = AmoledBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
