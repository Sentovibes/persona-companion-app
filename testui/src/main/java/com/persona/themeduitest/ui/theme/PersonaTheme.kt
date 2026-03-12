package com.persona.themeduitest.ui.theme

import androidx.compose.ui.graphics.Color

// Persona 3 Colors
object P3Colors {
    val Primary = Color(0xFF0A4D8C)        // Deep Blue
    val Secondary = Color(0xFF1E88E5)      // Bright Blue
    val Accent = Color(0xFF00D4FF)         // Cyan
    val Background = Color(0xFF0D1B2A)     // Dark Navy
    val Surface = Color(0xFF1A2332)        // Slightly lighter navy
    val OnPrimary = Color.White
    val OnSecondary = Color.White
    val OnBackground = Color.White
}

// Persona 4 Colors
object P4Colors {
    val Primary = Color(0xFFFFD700)        // Gold/Yellow
    val Secondary = Color(0xFFFFA500)      // Orange
    val Accent = Color(0xFFFF6B00)         // Bright Orange
    val Background = Color(0xFF2C2416)     // Dark Brown
    val Surface = Color(0xFF3D3020)        // Slightly lighter brown
    val OnPrimary = Color.Black
    val OnSecondary = Color.Black
    val OnBackground = Color.White
}

// Persona 5 Colors
object P5Colors {
    val Primary = Color(0xFFE60012)        // Phantom Red
    val Secondary = Color.Black
    val Accent = Color.White
    val Background = Color(0xFF1A1A1A)     // Dark Gray
    val Surface = Color(0xFF2A2A2A)        // Slightly lighter gray
    val OnPrimary = Color.White
    val OnSecondary = Color.White
    val OnBackground = Color.White
}

enum class PersonaGame {
    P3, P4, P5
}
