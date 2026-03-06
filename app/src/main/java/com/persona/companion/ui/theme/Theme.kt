package com.persona.companion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
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

@Composable
fun PersonaCompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
