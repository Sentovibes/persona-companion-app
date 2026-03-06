package com.persona.companion.models

import com.google.gson.annotations.SerializedName

/**
 * Universal Persona model capable of reading P3, P4, and P5 JSON structures.
 */
data class Persona(
    val name: String = "",
    @SerializedName("race") val arcana: String,
    @SerializedName("lvl") val level: Int,
    val stats: List<Int> = emptyList(),
    val skills: Map<String, Double> = emptyMap(),
    @SerializedName("resists") val resistsString: String = "",
    
    // Universal Optional Field
    val inherits: String? = null,
    
    // P5 Specific Fields (Default to null)
    val item: String? = null,
    val itemr: String? = null,
    val trait: String? = null,
    @SerializedName("special") val specialFusion: Boolean = false,
    
    // P3 Specific Fields (Default to null)
    val heart: String? = null,
    val cardlvl: Int? = null,
    
    val description: String = ""
) {
    val weaknesses: List<String> get() = parseElements('w')
    val resistances: List<String> get() = parseElements('s')
    val nullifies: List<String> get() = parseElements('n')
    val repels: List<String> get() = parseElements('r')
    val absorbs: List<String> get() = parseElements('d')

    private fun parseElements(type: Char): List<String> {
        // We use the maximum possible elements (P5) to prevent crashes
        val elements = listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
        if (resistsString.isEmpty()) return emptyList()
        
        return resistsString.mapIndexedNotNull { index, char ->
            if (char == type && index < elements.size) elements[index] else null
        }
    }
}
