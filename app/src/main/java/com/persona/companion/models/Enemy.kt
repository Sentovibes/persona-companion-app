package com.persona.companion.models

data class Enemy(
    val name: String,
    val persona_name: String? = null,  // For P5/P5R enemies - the actual demon name for images
    val arcana: String,
    val level: Int,
    val hp: Int,
    val sp: Int,
    val stats: EnemyStats? = null,
    val resists: String,
    val skills: List<String>,
    val area: String,
    val exp: Int = 0,
    val drops: EnemyDrops? = null,
    val date: String? = null,  // For main bosses with encounter dates
    val version: String? = null,  // For P5 bosses (Vanilla & Royal, Royal Exclusive, etc.)
    val phases: List<BossPhase>? = null,  // For multi-phase bosses
    val parts: List<BossPart>? = null,  // For bosses with multiple parts
    val isBoss: Boolean = false,
    val isMiniBoss: Boolean = false,
    val episodeAigis: Boolean? = null  // P3R: Episode Aigis (The Answer) enemies, hidden when the setting is off
) {
    val displayHp: String
        get() {
            if (hp > 0) return hp.toString()
            val phaseSum = phases?.sumOf { it.hp } ?: 0
            if (phaseSum > 0) return phaseSum.toString()
            val partSum = parts?.sumOf { it.hp } ?: 0
            if (partSum > 0) return partSum.toString()
            return "??"
        }

    val displaySp: String
        get() {
            if (sp > 0) return sp.toString()
            val phaseSum = phases?.sumOf { it.sp } ?: 0
            if (phaseSum > 0) return phaseSum.toString()
            val partSum = parts?.sumOf { it.sp ?: 0 } ?: 0
            if (partSum > 0) return partSum.toString()
            return "??"
        }

    fun getWeaknesses(gameId: String = ""): List<String> =
        com.persona.companion.utils.ResistanceUtils.getWeaknesses(resists, gameId)
}

data class EnemyStats(
    val strength: Int,
    val magic: Int,
    val endurance: Int,
    val agility: Int,
    val luck: Int
)

data class EnemyDrops(
    val gem: String,
    val item: String
)

data class BossData(
    val main_bosses: List<Enemy>,
    val mini_bosses: List<Enemy>
)

data class BossPhase(
    val name: String,
    val hp: Int,
    val sp: Int,
    val resists: String,
    val skills: List<String>,
    val parts: List<BossPart>? = null  // Some phases have parts (like Yaldabaoth)
)

data class BossPart(
    val name: String,
    val hp: Int,
    val sp: Int? = null,
    val resists: String,
    val skills: List<String>? = null
)
