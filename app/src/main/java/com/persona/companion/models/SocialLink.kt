package com.persona.companion.models

/**
 * Represents a Social Link/Confidant for a specific Arcana
 */
data class SocialLink(
    val arcana: String,
    val ranks: List<SocialLinkRank>,
    val details: SocialLinkDetails? = null,
    val isP4GExclusive: Boolean = false,
    val isP5RExclusive: Boolean = false
)

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
    val dialogues: List<SocialLinkDialogue> = emptyList()
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
    val isPhoneChoice: Boolean = false
)

/**
 * Container for all Social Links in a game
 */
data class SocialLinksData(
    val gameId: String,
    val socialLinks: List<SocialLink>
)
