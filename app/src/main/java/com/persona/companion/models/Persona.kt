package com.persona.companion.models

import com.google.gson.annotations.SerializedName

/**
 * Universal Persona model capable of reading P3, P4, and P5 JSON structures.
 */
data class Persona(
    val name: String = "",
    @SerializedName("race") val arcana: String? = null,
    @SerializedName("lvl") val level: Int? = null,
    val stats: List<Int>? = null,
    val skills: Map<String, Double>? = null,
    @SerializedName("resists") val resistsString: String? = null,
    
    // Universal Optional Field
    val inherits: String? = null,
    
    // P5 Specific Fields
    val item: String? = null,
    val itemr: String? = null,
    val trait: String? = null,
    @SerializedName("special") val specialFusion: Any? = null,
    val resmods: List<Int>? = null,
    
    // P3 Specific Fields
    val heart: String? = null,
    val cardlvl: Int? = null,
    
    val description: String? = null
) {
    val weaknesses: List<String> get() = parseElements('w')
    val resistances: List<String> get() = parseElements('s')
    val nullifies: List<String> get() = parseElements('n')
    val repels: List<String> get() = parseElements('r')
    val absorbs: List<String> get() = parseElements('d')

    private fun parseElements(type: Char): List<String> {
        val safeResists = resistsString ?: ""
        if (safeResists.isEmpty()) return emptyList()

        // Determine elements by specific game fields
        val isP3 = heart != null || cardlvl != null
        val isP5 = trait != null || item != null || itemr != null || resmods != null || specialFusion != null

        val elements = if (isP3) {
            listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
        } else if (isP5) {
            listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
        } else {
            // Fallback for P4 (usually 7 characters)
            if (safeResists.length == 7) {
                listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark")
            } else {
                listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
            }
        }
        
        return safeResists.mapIndexedNotNull { index, char ->
            if (char == type && index < elements.size) elements[index] else null
        }
    }
}