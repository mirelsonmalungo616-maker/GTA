package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GtaDarkColorScheme = darkColorScheme(
    primary = GtaGold,
    onPrimary = Color.Black,
    primaryContainer = GtaGoldDark,
    onPrimaryContainer = Color.White,
    secondary = GtaArmorBlue,
    onSecondary = Color.White,
    tertiary = GtaHealthGreen,
    onTertiary = Color.Black,
    background = GtaDarkBg,
    onBackground = TextPrimary,
    surface = GtaSurface,
    onSurface = TextPrimary,
    surfaceVariant = GtaSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GtaBorder,
    error = GtaDangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GtaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
