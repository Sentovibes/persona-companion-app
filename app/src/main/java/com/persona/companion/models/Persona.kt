package com.persona.companion.models

import com.google.gson.annotations.SerializedName

/**
 * Represents a single Persona's stats.
 * All values are base stats as they appear at the Persona's base level.
 */
data class PersonaStats(
    @SerializedName("strength")  val strength: Int = 0,
    @SerializedName("magic")     val magic: Int = 0,
    @SerializedName("endurance") val endurance: Int = 0,
    @SerializedName("agility")   val agility: Int = 0,
    @SerializedName("luck")      val luck: Int = 0
)

/**
 * A single skill entry — name plus the level it's learned at (0 = innate).
 */
data class SkillEntry(
    @SerializedName("name")  val name: String,
    @SerializedName("level") val level: Int = 0
)

/**
 * Core Persona model. Matches the JSON schema used in assets/data/.
 *
 * Fields are nullable where the data may be incomplete so the app
 * doesn't crash if a contributor only fills in partial info.
 */
data class Persona(
    @SerializedName("name")          val name: String,
    @SerializedName("arcana")        val arcana: String,
    @SerializedName("level")         val level: Int,
    @SerializedName("stats")         val stats: PersonaStats = PersonaStats(),
    @SerializedName("skills")        val skills: List<SkillEntry> = emptyList(),
    @SerializedName("description")   val description: String = "",
    @SerializedName("weaknesses")    val weaknesses: List<String> = emptyList(),
    @SerializedName("resistances")   val resistances: List<String> = emptyList(),
    @SerializedName("nullifies")     val nullifies: List<String> = emptyList(),
    @SerializedName("repels")        val repels: List<String> = emptyList(),
    @SerializedName("absorbs")       val absorbs: List<String> = emptyList(),
    @SerializedName("special_fusion") val specialFusion: Boolean = false
)
