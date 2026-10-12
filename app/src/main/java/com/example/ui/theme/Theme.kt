package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LimeAccent,
    onPrimary = DeepGreen,
    primaryContainer = ForestGreen,
    onPrimaryContainer = LimeAccent,
    secondary = LimeAccent,
    onSecondary = DeepGreen,
    tertiary = ForestGreen,
    onTertiary = SoftCream,
    background = DarkBackground,
    onBackground = SoftCream,
    surface = DarkSurface,
    onSurface = SoftCream,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = DarkMutedText,
    outline = DarkBorder,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = BrandWhite,
    primaryContainer = ForestGreen,
    onPrimaryContainer = LimeAccent,
    secondary = DeepGreen,
    onSecondary = BrandWhite,
    secondaryContainer = LimeAccent,
    onSecondaryContainer = DeepGreen,
    tertiary = LimeAccent,
    onTertiary = DeepGreen,
    background = SoftCream,
    onBackground = DeepGreen,
    surface = BrandWhite,
    onSurface = DeepGreen,
    surfaceVariant = SoftCream,
    onSurfaceVariant = MutedText,
    outline = SubtleBorder,
    error = ExpenseRed,
    onError = BrandWhite
)

@Composable
fun PennywiseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
