package com.persona.companion.models

import androidx.compose.ui.graphics.Color

/**
 * A single game release within a series.
 */
data class Game(
    val id: String,
    val title: String,
    val dataPath: String
)

/**
 * One of the main Persona series (P3, P4, P5).
 */
data class PersonaSeries(
    val id: String,
    val title: String,
    val color: Color,
    val games: List<Game>
)
