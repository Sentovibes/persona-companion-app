package com.persona.companion.models

import androidx.compose.ui.graphics.Color

data class Game(
    val id: String,
    val title: String,
    val dataPath: String,
    val enemyPath: String? = null,
    val skillPath: String? = null,
    val itemPath: String? = null,
    val socialLinkPath: String? = null,
    val requestPath: String? = null,
    val aigisDataPath: String? = null,
    val aigisEnemyPath: String? = null
)

data class PersonaSeries(
    val id: String,
    val title: String,
    val color: Color,
    val games: List<Game>
)
