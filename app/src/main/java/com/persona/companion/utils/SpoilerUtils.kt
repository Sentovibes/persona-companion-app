package com.persona.companion.utils

/**
 * Utility to identify sensitive late-game storyline spoilers
 * for No-Spoilers Mode (Bosses, Personas, Social Links, and Quests).
 */
object SpoilerUtils {

    // Story bosses that reveal secret villains, traitors, or final deities
    private val SPOILER_BOSS_NAMES = setOf(
        // Persona 3
        "ryoji mochizuki", "jin", "takaya", "chidori", "nyx", "nyx avatar", "erebus",
        // Persona 4
        "shadow rise", "shadow mitsuo", "kunino-sagiri", "tohru adachi", "magatsu-izanagi",
        "ameno-sagiri", "marie", "kusumi-no-okami", "izanami", "izanami-no-okami",
        // Persona 5 / Royal
        "goro akechi", "crow", "loki", "robin hood", "cognitive akechi",
        "masayoshi shido", "samael", "beast of human sacrifice", "tomb of human sacrifice", "wings of human sacrifice",
        "holy grail", "yaldabaoth", "god of control",
        "takuto maruki", "azathoth", "adam kadmon", "kasumi yoshizawa", "cendrillon",
        "lavenza", "caroline & justine"
    )

    // Personas tied to endgame/plot-twist revelations
    private val SPOILER_PERSONA_NAMES = setOf(
        "satanael", "izanagi-no-okami", "izanagi-no-okami picaro",
        "messiah", "messiah picaro", "thanatos",
        "loki", "magatsu-izanagi", "magatsu-izanagi picaro",
        "lucifer", "helel", "raoul", "hereward", "ella", "kaguya", "kaguya picaro"
    )

    // Confidants / Social Links with late-game story identities
    private val SPOILER_CONFIDANTS = setOf(
        "jester", "hunger", "councillor", "faith", "judgment", "judgement"
    )

    fun isSpoilerBoss(name: String): Boolean {
        val lower = name.trim().lowercase()
        return SPOILER_BOSS_NAMES.any { lower.contains(it) }
    }

    fun isSpoilerPersona(name: String): Boolean {
        val lower = name.trim().lowercase()
        return SPOILER_PERSONA_NAMES.contains(lower)
    }

    fun isSpoilerSocialLink(arcanaOrName: String): Boolean {
        val lower = arcanaOrName.trim().lowercase()
        return SPOILER_CONFIDANTS.any { lower.contains(it) }
    }
}
