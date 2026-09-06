package com.persona.companion.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WearBlack = Color(0xFF000000)
val WearDarkGray = Color(0xFF18181B)
val WearMediumGray = Color(0xFF27272A)
val WearLightGray = Color(0xFFA1A1AA)
val WearWhite = Color(0xFFFFFFFF)

// Persona Accent Colors
val P3Blue = Color(0xFF2979FF)
val P4Gold = Color(0xFFFFC107)
val P5Red = Color(0xFFFF1744)

// Resistance Affinity Colors
val WeakColor = Color(0xFFFF5252)
val ResistColor = Color(0xFF448AFF)
val NullColor = Color(0xFF78909C)
val RepelColor = Color(0xFF69F0AE)
val DrainColor = Color(0xFF40C4FF)

fun getGameAccentColor(gameId: String): Color {
    return when {
        gameId.startsWith("p3") -> P3Blue
        gameId.startsWith("p4") -> P4Gold
        gameId.startsWith("p5") -> P5Red
        else -> P4Gold
    }
}

val WearColorPalette = Colors(
    primary = Color(0xFFFFC107),
    primaryVariant = Color(0xFFFFA000),
    secondary = Color(0xFF2979FF),
    background = WearBlack,
    surface = WearDarkGray,
    onPrimary = WearBlack,
    onSecondary = WearWhite,
    onBackground = WearWhite,
    onSurface = WearWhite,
    onError = WearWhite
)

@Composable
fun PersonaWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorPalette,
        content = content
    )
}
