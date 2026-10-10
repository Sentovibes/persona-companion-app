package com.persona.companion.models

/**
 * Result model for unified Global Search across all games and categories.
 */
data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val category: SearchCategory,
    val seriesId: String,
    val gameId: String,
    val gameTitle: String,
    val destinationRoute: String,
    val isSpoiler: Boolean = false,
    val extraInfo: String? = null
)

enum class SearchCategory(val label: String) {
    PERSONA("Personas"),
    ENEMY("Enemies & Bosses"),
    SKILL("Skills"),
    ITEM("Items"),
    SOCIAL_LINK("Social Links"),
    QUEST("Requests"),
    GUIDE("Guides")
}
