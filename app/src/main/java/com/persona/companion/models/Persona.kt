package com.persona.companion.models

import com.google.gson.annotations.SerializedName

/**
 * Core Persona model updated to match your JSON schema.
 */
data class Persona(
    val name: String = "",
    @SerializedName("race") val arcana: String,
    @SerializedName("lvl") val level: Int,
    val stats: List<Int> = emptyList(),
    // JSON skills are "Name": Level (e.g. "Mabufudyne": 0.1)
    val skills: Map<String, Double> = emptyMap(),
    @SerializedName("resists") val resistsString: String = "",
    val trait: String? = null,
    val description: String = "",
    @SerializedName("special") val specialFusion: Boolean = false
) {
    val weaknesses: List<String> get() = parseElements('w')
    val resistances: List<String> get() = parseElements('s')
    val nullifies: List<String> get() = parseElements('n')
    val repels: List<String> get() = parseElements('r')
    val absorbs: List<String> get() = parseElements('d')

    private fun parseElements(type: Char): List<String> {
        val elements = listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
        return resistsString.mapIndexedNotNull { index, char ->
            if (char == type && index < elements.size) elements[index] else null
        }
    }
}
