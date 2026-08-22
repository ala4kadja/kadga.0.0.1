package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KadjaDarkColorScheme = darkColorScheme(
    primary = KadjaPrimary,
    onPrimary = KadjaPrimaryDark,
    primaryContainer = KadjaPrimaryVariant,
    onPrimaryContainer = KadjaPrimaryGlow,
    secondary = KadjaSecondary,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = KadjaSecondaryVariant,
    onSecondaryContainer = KadjaSecondary,
    tertiary = KadjaTertiary,
    onTertiary = Color(0xFF492532),
    background = KadjaBackground,
    onBackground = KadjaTextPrimary,
    surface = KadjaSurface,
    onSurface = KadjaTextPrimary,
    surfaceVariant = KadjaSurfaceVariant,
    onSurfaceVariant = KadjaTextSecondary,
    surfaceTint = KadjaPrimary,
    outline = KadjaCardBorder,
    outlineVariant = KadjaSurfaceElevated,
    error = KadjaError,
    onError = Color(0xFF601410)
)

@Composable
fun KadjaPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KadjaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
