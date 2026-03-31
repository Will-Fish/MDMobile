package com.example.mdmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),
    secondary = Color(0xFFF472B6),
    tertiary = Color(0xFF60A5FA),
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFF2A2A2A),
    error = Color(0xFFF87171),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF0F0F0),
    onSurface = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF404040)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    secondary = Color(0xFFDB2777),
    tertiary = Color(0xFF2563EB),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F5F5),
    error = Color(0xFFDC2626),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE5E5E5)
)

@Composable
fun MDMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}