package com.persona.companion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme colors (current)
private val DarkColorScheme = darkColorScheme(
    primary           = Color(0xFFBB86FC),
    onPrimary         = Color(0xFF000000),
    primaryContainer  = Color(0xFF3700B3),
    secondary         = Color(0xFF03DAC6),
    onSecondary       = Color(0xFF000000),
    background        = Background,
    onBackground      = TextPrimary,
    surface           = Surface,
    onSurface         = TextPrimary,
    surfaceVariant    = SurfaceCard,
    onSurfaceVariant  = TextSecondary,
    outline           = Divider,
    error             = Color(0xFFCF6679),
    onError           = Color(0xFF000000)
)

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary           = Color(0xFF6200EE),
    onPrimary         = Color(0xFFFFFFFF),
    primaryContainer  = Color(0xFFBB86FC),
    secondary         = Color(0xFF03DAC6),
    onSecondary       = Color(0xFF000000),
    background        = Color(0xFFF5F5F5),
    onBackground      = Color(0xFF1C1C1E),
    surface           = Color(0xFFFFFFFF),
    onSurface         = Color(0xFF1C1C1E),
    surfaceVariant    = Color(0xFFF0F0F0),
    onSurfaceVariant  = Color(0xFF666666),
    outline           = Color(0xFFE0E0E0),
    error             = Color(0xFFB00020),
    onError           = Color(0xFFFFFFFF)
)

@Composable
fun PersonaCompanionTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
