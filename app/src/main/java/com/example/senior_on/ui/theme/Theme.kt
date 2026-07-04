package com.example.senior_on.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SeniorOnColors.Primary600,
    secondary = SeniorOnColors.Communication,
    tertiary = SeniorOnColors.Health,
    background = SeniorOnColors.Gray800,
    surface = SeniorOnColors.Gray800,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SeniorOnColors.Primary600,
    secondary = SeniorOnColors.Communication,
    tertiary = SeniorOnColors.Health,
    background = SeniorOnColors.Background1,
    surface = SeniorOnColors.White,
    error = SeniorOnColors.Red500,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SeniorOnColors.Gray800,
    onSurface = SeniorOnColors.Gray800,
    onError = Color.White
)

@Composable
fun SENIOR_ONTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
