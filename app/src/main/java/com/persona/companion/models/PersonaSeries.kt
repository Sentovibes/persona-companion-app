package com.persona.companion.models

import androidx.compose.ui.graphics.Color

/**
 * A single game release within a series (e.g. "Persona 5 Royal").
 *
 * @param id        Stable identifier used in navigation routes.
 * @param title     Human-readable display name.
 * @param dataPath  Path inside assets/ pointing to the personas JSON file.
 */
data class Game(
    val id: String,
    val title: String,
    val dataPath: String
)

/**
 * One of the three main Persona series (P3, P4, P5).
 *
 * @param id       Stable identifier — "p3", "p4", or "p5".
 * @param title    Display name shown on the home screen.
 * @param color    Accent color associated with this series.
 * @param games    All game releases that belong to this series.
 */
data class PersonaSeries(
    val id: String,
    val title: String,
    val color: Color,
    val games: List<Game>
)
