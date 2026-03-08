package com.persona.companion.models

enum class PersonaSortOption {
    LEVEL_ASC,
    LEVEL_DESC,
    NAME_ASC,
    NAME_DESC,
    ARCANA,
    STRENGTH,
    MAGIC,
    ENDURANCE,
    AGILITY,
    LUCK
}

enum class EnemySortOption {
    LEVEL_ASC,
    LEVEL_DESC,
    NAME_ASC,
    NAME_DESC,
    HP_ASC,
    HP_DESC
}

enum class SkillType {
    ALL,
    PHYSICAL,
    MAGIC,
    HEALING,
    SUPPORT,
    PASSIVE,
    ALMIGHTY
}

enum class ResistanceFilter {
    ALL,
    WEAK,
    RESIST,
    NULL,
    DRAIN
}

data class PersonaFilters(
    val sortOption: PersonaSortOption = PersonaSortOption.LEVEL_ASC,
    val skillType: SkillType = SkillType.ALL,
    val minLevel: Int = 1,
    val maxLevel: Int = 99,
    val gameExclusive: Boolean = false,
    val dlcOnly: Boolean = false,
    val showFavoritesOnly: Boolean = false
)

data class EnemyFilters(
    val sortOption: EnemySortOption = EnemySortOption.LEVEL_ASC,
    val resistanceType: String? = null, // e.g., "fire", "ice"
    val resistanceFilter: ResistanceFilter = ResistanceFilter.ALL,
    val minLevel: Int = 1,
    val maxLevel: Int = 99,
    val gameExclusive: Boolean = false,
    val showFavoritesOnly: Boolean = false
)
