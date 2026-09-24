package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FloppiaColorScheme = darkColorScheme(
    primary = NeonOrange,
    onPrimary = Color.Black,
    primaryContainer = NeonOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = MinecraftGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1B3B1B),
    onSecondaryContainer = MinecraftGreen,
    tertiary = MinecraftGold,
    onTertiary = Color.Black,
    background = FloppiaBackground,
    onBackground = TextPrimary,
    surface = FloppiaSurface,
    onSurface = TextPrimary,
    surfaceVariant = FloppiaSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = FloppiaCardBorder,
    error = MinecraftRed,
    onError = Color.White
)

@Composable
fun FloppiaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FloppiaColorScheme,
        typography = Typography,
        content = content
    )
}
