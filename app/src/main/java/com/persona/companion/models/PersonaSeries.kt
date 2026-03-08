package com.persona.companion.models

import androidx.compose.ui.graphics.Color

data class Game(
    val id: String,
    val title: String,
    val dataPath: String,
    val enemyPath: String? = null
)

data class PersonaSeries(
    val id: String,
    val title: String,
    val color: Color,
    val games: List<Game>
)
