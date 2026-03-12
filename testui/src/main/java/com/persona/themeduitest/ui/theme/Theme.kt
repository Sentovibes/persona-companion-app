package com.persona.themeduitest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = P3Colors.Primary,
    secondary = P3Colors.Secondary,
    tertiary = P3Colors.Accent,
    background = P3Colors.Background,
    surface = P3Colors.Surface,
    onPrimary = P3Colors.OnPrimary,
    onSecondary = P3Colors.OnSecondary,
    onBackground = P3Colors.OnBackground
)

@Composable
fun PersonaThemedUITestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
