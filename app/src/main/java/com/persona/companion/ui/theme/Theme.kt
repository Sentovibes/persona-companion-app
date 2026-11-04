package com.persona.companion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary            = AccentBrass,
    onPrimary          = Color(0xFF0D0D12),
    primaryContainer   = SurfaceRaised,
    onPrimaryContainer = TextPrimary,
    secondary          = AccentBlue,
    onSecondary        = Color(0xFF0D0D12),
    background         = Background,
    onBackground       = TextPrimary,
    surface            = Surface,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceCard,
    onSurfaceVariant   = TextSecondary,
    outline            = HairlineStrong,
    error              = AccentRed,
    onError            = Color(0xFF0D0D12)
)

@Composable
fun PersonaCompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
