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
    val resists: Any? = null,  // Can be String (old format) or List<String> (P3R format)
    
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
    
    // P3 Reload Format Fields
    val id: Int? = null,
    val description: String? = null,
    val image: String? = null,
    val strength: Int? = null,
    val magic: Int? = null,
    val endurance: Int? = null,
    val agility: Int? = null,
    val luck: Int? = null,
    val weak: List<String>? = null,
    val reflects: List<String>? = null,
    @SerializedName("absorbs") val absorbsJson: List<String>? = null,
    @SerializedName("nullifies") val nullifiesJson: List<String>? = null,
    val dlc: Int? = null
) {
    val weaknesses: List<String> get() {
        // P3 Reload format uses direct lists
        if (weak != null) return weak
        return parseElements('w')
    }
    
    val resistances: List<String> get() {
        // P3 Reload format uses direct lists - resists is a List<String>
        if (resists is List<*>) {
            @Suppress("UNCHECKED_CAST")
            return resists as? List<String> ?: emptyList()
        }
        return parseElements('s')
    }
    
    val nullifies: List<String> get() {
        // P3 Reload format uses direct lists
        if (nullifiesJson != null) return nullifiesJson
        return parseElements('n')
    }
    
    val repels: List<String> get() {
        // P3 Reload format uses direct lists
        if (reflects != null) return reflects
        return parseElements('r')
    }
    
    val absorbs: List<String> get() {
        // P3 Reload format uses direct lists
        if (absorbsJson != null) return absorbsJson
        return parseElements('d')
    }

    private fun parseElements(type: Char): List<String> {
        // Get resists as string for old format
        val safeResists = when (resists) {
            is String -> resists
            else -> ""
        }
        if (safeResists.isEmpty()) return emptyList()

        // Determine elements by specific game fields
        val isP3 = heart != null || cardlvl != null
        val isP5 = trait != null || item != null || itemr != null || resmods != null || specialFusion != null

        val elements = if (isP3) {
            listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
        } else if (isP5) {
            listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
        } else {
            // Fallback for P4 (usually 7 or 8 characters)
            if (safeResists.length == 7) {
                listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark")
            } else if (safeResists.length == 8) {
                listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
            } else {
                listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
            }
        }
        
        return safeResists.mapIndexedNotNull { index, char ->
            if (char == type && index < elements.size) elements[index] else null
        }
    }
}