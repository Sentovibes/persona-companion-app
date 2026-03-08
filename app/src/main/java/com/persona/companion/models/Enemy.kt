package com.persona.companion.models

data class Enemy(
    val name: String,
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
    val isMiniBoss: Boolean = false
)

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
