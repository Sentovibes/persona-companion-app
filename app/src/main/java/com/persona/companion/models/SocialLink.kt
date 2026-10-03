package com.persona.companion.models

/**
 * Represents a Social Link/Confidant for a specific Arcana
 */
data class SocialLink(
    val arcana: String,
    val characterName: String? = null,
    val ranks: List<SocialLinkRank>,
    val details: SocialLinkDetails? = null,
    val isP4GExclusive: Boolean = false,
    val isP5RExclusive: Boolean = false,
    val ultimatePersona: String? = null,
    val thirdAwakening: ThirdAwakening? = null
) {
    val hasRouteBranches: Boolean
        get() = ranks.any { it.isRomanceRoute } || ranks.any { it.isPlatonicRoute }
}

/**
 * Schedule/location info shown at the top of a social link
 */
data class SocialLinkDetails(
    val trigger: String? = null,
    val schedule: String? = null,
    val timeOfDay: String? = null,
    val location: String? = null
)

/**
 * Represents a single rank in a Social Link
 */
data class SocialLinkRank(
    val rankNumber: Int,
    val rankName: String,
    val isAuto: Boolean,
    val nextRankPoints: Int = 0,
    val requirements: String? = null,
    val benefit: SocialLinkBenefit? = null,
    val unlocks: List<String>? = null,
    val dialogues: List<SocialLinkDialogue> = emptyList()
) {
    val isRomanceRoute: Boolean
        get() = rankName.contains("Romance", ignoreCase = true) || rankName.contains("Romantic", ignoreCase = true)

    val isPlatonicRoute: Boolean
        get() = rankName.contains("Platonic", ignoreCase = true)

    val cleanRankTitle: String
        get() {
            val cleaned = rankName
                .replace(Regex("(?i)\\s*Romant(ic|e)\\s*"), " ")
                .replace(Regex("(?i)\\s*Platonic\\s*"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            return cleaned.ifEmpty { rankName }
        }
}

/**
 * Represents a specific ability or benefit unlocked at a rank
 */
data class SocialLinkBenefit(
    val name: String,
    val description: String
)

/**
 * Represents a final Persona evolution (P4G Winter / P5R Third Semester)
 */
data class ThirdAwakening(
    val persona: String,
    val name: String? = null,
    val description: String? = null,
    val requirement: String? = null
)

/**
 * A dialogue prompt with one or more answer choices
 */
data class SocialLinkDialogue(
    val question: String,
    val choices: List<DialogueChoice>
)

/**
 * Represents a dialogue choice with its point value
 */
data class DialogueChoice(
    val text: String,
    val points: Int,
    val isPhoneChoice: Boolean = false,
    val flag: String? = null
) {
    val isRomanceFlag: Boolean
        get() = flag?.contains("Romance", ignoreCase = true) == true ||
                text.contains("Triggers Romance", ignoreCase = true) ||
                text.contains("Romance Flag", ignoreCase = true)

    val isPlatonicFlag: Boolean
        get() = flag?.contains("Platonic", ignoreCase = true) == true ||
                text.contains("Locks Platonic", ignoreCase = true) ||
                text.contains("Romance Flag A avoided", ignoreCase = true)

    val isOptimal: Boolean
        get() = flag?.contains("Optimal", ignoreCase = true) == true ||
                text.contains("Optimal", ignoreCase = true)
}

/**
 * Container for all Social Links in a game
 */
data class SocialLinksData(
    val gameId: String,
    val socialLinks: List<SocialLink>
)

