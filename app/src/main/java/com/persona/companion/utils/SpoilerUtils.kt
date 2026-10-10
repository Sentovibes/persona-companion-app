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

    // Confidants / Social Links with late-game story identities or twists
    private val SPOILER_CONFIDANT_ARCANAS = setOf(
        "jester", "hunger", "councillor", "faith", "judgment", "judgement"
    )

    private val SPOILER_CONFIDANT_CHARACTERS = setOf(
        "akechi", "goro akechi", "maruki", "takuto maruki", "adachi", "tohru adachi",
        "kasumi", "sumire", "kasumi yoshizawa", "sumire yoshizawa",
        "ryoji", "ryoji mochizuki", "pharos", "nyx"
    )

    fun isSpoilerBoss(name: String): Boolean {
        val lower = name.trim().lowercase()
        return SPOILER_BOSS_NAMES.any { lower.contains(it) }
    }

    fun isSpoilerPersona(name: String): Boolean {
        val lower = name.trim().lowercase()
        return SPOILER_PERSONA_NAMES.contains(lower)
    }

    fun isSpoilerSocialLink(arcanaOrName: String, characterName: String? = null): Boolean {
        val arcanaLower = arcanaOrName.trim().lowercase()
        if (SPOILER_CONFIDANT_ARCANAS.any { arcanaLower.contains(it) }) return true
        if (SPOILER_CONFIDANT_CHARACTERS.any { arcanaLower.contains(it) }) return true
        if (!characterName.isNullOrBlank()) {
            val charLower = characterName.trim().lowercase()
            if (SPOILER_CONFIDANT_CHARACTERS.any { charLower.contains(it) }) return true
        }
        return false
    }
}
