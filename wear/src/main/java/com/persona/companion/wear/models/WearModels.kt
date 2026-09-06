package com.persona.companion.wear.models

data class WearGame(
    val id: String,
    val title: String,
    val shortTitle: String,
    val colorHex: Long,
    val series: String
)

val WEAR_GAMES = listOf(
    WearGame("p4g", "Persona 4 Golden", "P4G", 0xFFFFC107, "p4"),
    WearGame("p5r", "Persona 5 Royal", "P5R", 0xFFFF1744, "p5"),
    WearGame("p3r", "Persona 3 Reload", "P3R", 0xFF2979FF, "p3"),
    WearGame("p4", "Persona 4", "P4", 0xFFFFB300, "p4"),
    WearGame("p5", "Persona 5", "P5", 0xFFD50000, "p5"),
    WearGame("p3p", "Persona 3 Portable", "P3P", 0xFF00B0FF, "p3"),
    WearGame("p3fes", "Persona 3 FES", "P3FES", 0xFF0091EA, "p3")
)

data class WearClassroomItem(
    val date: String,
    val question: String,
    val answer: String,
    val isExam: Boolean = false
)

data class WearEnemyItem(
    val name: String,
    val arcana: String,
    val level: Int,
    val weaknesses: List<String>,
    val resistances: List<Pair<String, String>>, // Element to Type (e.g. Fire to Weak)
    val area: String = ""
)

data class WearSocialLink(
    val arcana: String,
    val characterName: String,
    val ranks: List<WearSocialLinkRank>
)

data class WearSocialLinkRank(
    val rank: Int,
    val rankName: String,
    val dialogues: List<WearDialogue>
)

data class WearDialogue(
    val question: String,
    val bestChoices: List<WearChoice>
)

data class WearChoice(
    val text: String,
    val points: Int,
    val isPhone: Boolean = false
)
