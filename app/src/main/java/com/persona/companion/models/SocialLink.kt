package com.persona.companion.models

/**
 * Represents a Social Link/Confidant for a specific Arcana
 */
data class SocialLink(
    val arcana: String,
    val ranks: List<SocialLinkRank>,
    val isP4GExclusive: Boolean = false,  // True for Aeon and Jester in P4G
    val isP5RExclusive: Boolean = false   // True for Faith and Councillor in P5R
)

/**
 * Represents a single rank in a Social Link
 */
data class SocialLinkRank(
    val rankNumber: Int,
    val rankName: String,  // e.g., "Rank 1", "Rank 2", etc.
    val isAuto: Boolean,   // True if rank up is automatic
    val nextRankPoints: Int = 0,  // Points needed for next rank
    val requirements: String? = null,  // Requirements to unlock/progress
    val choices: List<DialogueChoice> = emptyList(),
    val location: String? = null,
    val availability: String? = null
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
