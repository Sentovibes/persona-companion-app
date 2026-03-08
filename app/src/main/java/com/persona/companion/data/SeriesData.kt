package com.persona.companion.data

import androidx.compose.ui.graphics.Color
import com.persona.companion.models.Game
import com.persona.companion.models.PersonaSeries

/**
 * Single source of truth for the game catalogue.
 *
 * To add a new game:
 *   1. Add a [Game] entry with a unique id and the correct dataPath.
 *   2. Drop the corresponding JSON file into assets/data/<series>/.
 *   3. That's it — the rest of the app picks it up automatically.
 */
object SeriesData {

    val allSeries: List<PersonaSeries> = listOf(

        PersonaSeries(
            id = "p3",
            title = "Persona 3",
            color = Color(0xFF1565C0),
            games = listOf(
                Game(
                    id = "p3fes",
                    title = "Persona 3 FES",
                    dataPath = "data/persona3/personas.json",
                    enemyPath = "data/enemies/p3fes_enemies.json"
                ),
                Game(
                    id = "p3p",
                    title = "Persona 3 Portable",
                    dataPath = "data/persona3/portable_personas.json",
                    enemyPath = "data/enemies/p3p_enemies.json"
                ),
                Game(
                    id = "p3r",
                    title = "Persona 3 Reload",
                    dataPath = "data/persona3/reload_personas.json",
                    enemyPath = "data/enemies/p3r_enemies.json"
                )
            )
        ),

        PersonaSeries(
            id = "p4",
            title = "Persona 4",
            color = Color(0xFFF9A825),
            games = listOf(
                Game(
                    id = "p4",
                    title = "Persona 4",
                    dataPath = "data/persona4/personas.json",
                    enemyPath = "data/enemies/p4_enemies.json"
                ),
                Game(
                    id = "p4g",
                    title = "Persona 4 Golden",
                    dataPath = "data/persona4/golden_personas.json",
                    enemyPath = "data/enemies/p4g_enemies.json"
                )
            )
        ),

        PersonaSeries(
            id = "p5",
            title = "Persona 5",
            color = Color(0xFFC62828),
            games = listOf(
                Game(
                    id = "p5",
                    title = "Persona 5",
                    dataPath = "data/persona5/personas.json",
                    enemyPath = "data/enemies/p5_enemies.json"
                ),
                Game(
                    id = "p5r",
                    title = "Persona 5 Royal",
                    dataPath = "data/persona5/royal_personas.json",
                    enemyPath = "data/enemies/p5r_enemies.json"
                ),
                Game(
                    id = "p5s",
                    title = "Persona 5 Strikers",
                    dataPath = "data/persona5/personas.json",
                    enemyPath = "data/enemies/p5s_enemies.json"
                )
            )
        )
    )

    fun findSeries(seriesId: String): PersonaSeries? = allSeries.find { it.id == seriesId }

    fun findGame(seriesId: String, gameId: String): Game? =
        findSeries(seriesId)?.games?.find { it.id == gameId }
}
