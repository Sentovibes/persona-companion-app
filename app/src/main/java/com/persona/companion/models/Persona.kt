package com.persona.companion.models

import com.google.gson.annotations.SerializedName

/**
 * Universal Persona model capable of reading P3, P4, and P5 JSON structures.
 */
data class Persona(
    val name: String = "",
    @SerializedName("race", alternate = ["arcana"]) val arcana: String? = null,
    @SerializedName("lvl", alternate = ["level"]) val level: Int? = null,
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
    val dlc: Int? = null,
    
    // Unlock requirements and special flags
    val unlock: String? = null,
    val episodeAigis: Boolean? = null,
    val isDlc: Boolean? = null
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
        
        // P3 has 10 elements: Slash, Strike, Pierce, Fire, Ice, Elec, Wind, Light, Dark, Almighty
        // P5 has 10 elements: Phys, Gun, Fire, Ice, Elec, Wind, Psy, Nuke, Bless, Curse
        // P4 has 7-8 elements: Phys, Fire, Ice, Elec, Wind, Light, Dark(, Almighty)
        
        val elements = when {
            isP3 -> listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
            isP5 -> listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
            safeResists.length == 10 && !isP5 -> {
                // 10 characters but not P5 = must be P3 (some personas don't have heart field)
                listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
            }
            safeResists.length == 7 -> listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark")
            safeResists.length == 8 -> listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
            else -> listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")
        }
        
        return safeResists.mapIndexedNotNull { index, char ->
            if (char == type && index < elements.size) elements[index] else null
        }
    }
}