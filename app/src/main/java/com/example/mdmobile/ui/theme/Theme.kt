package com.example.mdmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.mdmobile.data.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB8FF),
    onPrimary = Color(0xFF06254A),
    primaryContainer = Color(0xFF10345E),
    onPrimaryContainer = Color(0xFFD9E8FF),
    secondary = Color(0xFF5B87C8),
    onSecondary = Color(0xFFF5F9FF),
    secondaryContainer = Color(0xFF17345A),
    onSecondaryContainer = Color(0xFFD9E6FF),
    tertiary = Color(0xFF9FC4FF),
    onTertiary = Color(0xFF0A2445),
    background = Color(0xFF07111F),
    onBackground = Color(0xFFEAF2FF),
    surface = Color(0xFF0E1B2D),
    onSurface = Color(0xFFEAF2FF),
    surfaceVariant = Color(0xFF16263D),
    onSurfaceVariant = Color(0xFFADC1DE),
    outline = Color(0xFF35506F),
    error = Color(0xFFFF8C8C)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1859B8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E8FF),
    onPrimaryContainer = Color(0xFF092B57),
    secondary = Color(0xFF5379A7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1ECFF),
    onSecondaryContainer = Color(0xFF18304F),
    tertiary = Color(0xFF2F6CCB),
    onTertiary = Color.White,
    background = Color(0xFFF3F7FD),
    onBackground = Color(0xFF0E223D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF11253F),
    surfaceVariant = Color(0xFFE9F0F9),
    onSurfaceVariant = Color(0xFF5A6F89),
    outline = Color(0xFFC8D5E6),
    error = Color(0xFFD73E36)
)

@Composable
fun MDMobileTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
