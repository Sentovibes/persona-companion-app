package com.persona.companion.utils

object ResistanceUtils {

    // Element order by game — single source of truth for every resist-string parser.
    // All P3 games (FES, Portable, Reload) use the same 10-column order.
    private val P3_ELEMENTS = listOf("Slash", "Strike", "Pierce", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
    private val P4_ELEMENTS = listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", "Almighty")
    private val P5_ELEMENTS = listOf("Phys", "Gun", "Fire", "Ice", "Elec", "Wind", "Psy", "Nuke", "Bless", "Curse")

    private const val NORMAL = '-'

    /**
     * Get element names for a specific game
     */
    fun getElementNames(gameId: String): List<String> {
        return when {
            gameId.startsWith("p3") -> P3_ELEMENTS
            gameId.startsWith("p4") -> P4_ELEMENTS
            gameId.startsWith("p5") -> P5_ELEMENTS
            else -> P4_ELEMENTS // Default fallback
        }
    }

    /**
     * Get resistance label for a character code.
     *
     * Data files mix lowercase codes with uppercase typos ('W', 'N', 'A', 'D', 'S'),
     * which mean the same as their lowercase forms — EXCEPT 'R', which historically
     * means Repel while 'r' means Resist. Match explicitly; do not lowercase first.
     * Unknown codes return "" and are skipped by callers.
     */
    fun getResistanceLabel(code: Char): String {
        return when (code) {
            'w', 'W' -> "Weak"
            'r', 's', 'S' -> "Resist"
            'n', 'N', '_' -> "Null"
            'p', 'P', 'R' -> "Repel"
            'd', 'D', 'a', 'A' -> "Absorb"
            else -> ""
        }
    }

    /**
     * Elements this resist string is weak to, in element order.
     */
    fun getWeaknesses(resistString: String, gameId: String): List<String> {
        val elements = getElementNames(gameId)
        return resistString.mapIndexedNotNull { index, code ->
            if ((code == 'w' || code == 'W') && index < elements.size) elements[index] else null
        }
    }

    /**
     * Parse resistance string into a formatted list
     * Returns list of "Element: Resistance" strings (e.g., "Fire: Weak", "Ice: Resist")
     */
    fun formatResistances(resistString: String, gameId: String): List<String> {
        val elements = getElementNames(gameId)
        val result = mutableListOf<String>()

        resistString.forEachIndexed { index, code ->
            if (index < elements.size && code != NORMAL) {
                val label = getResistanceLabel(code)
                if (label.isNotEmpty()) {
                    result.add("${elements[index]}: $label")
                }
            }
        }

        return result
    }

    /**
     * Format resistances grouped by type
     * Returns map of resistance type to list of elements
     */
    fun formatResistancesGrouped(resistString: String, gameId: String): Map<String, List<String>> {
        val elements = getElementNames(gameId)
        val grouped = mutableMapOf<String, MutableList<String>>()

        resistString.forEachIndexed { index, code ->
            if (index < elements.size && code != NORMAL) {
                val label = getResistanceLabel(code)
                if (label.isNotEmpty()) {
                    grouped.getOrPut(label) { mutableListOf() }.add(elements[index])
                }
            }
        }

        return grouped
    }

    /**
     * Format resistances for sharing (compact format)
     * Example: "Weak: Fire, Ice | Resist: Elec, Wind | Null: Light"
     */
    fun formatResistancesForSharing(resistString: String, gameId: String): String {
        val grouped = formatResistancesGrouped(resistString, gameId)
        if (grouped.isEmpty()) return ""

        return grouped.entries
            .sortedBy {
                // Sort order: Weak, Resist, Null, Repel, Absorb
                when (it.key) {
                    "Weak" -> 1
                    "Resist" -> 2
                    "Null" -> 3
                    "Repel" -> 4
                    "Absorb" -> 5
                    else -> 6
                }
            }
            .joinToString(" | ") { (type, elements) ->
                "$type: ${elements.joinToString(", ")}"
            }
    }

    /**
     * Format resistances for sharing with Unicode symbols
     * Example: "🔥 Fire: Weak | ❄️ Ice: Resist | ⚡ Elec: Null"
     */
    fun formatResistancesWithIcons(resistString: String, gameId: String): String {
        val elements = getElementNames(gameId)
        val result = mutableListOf<String>()

        resistString.forEachIndexed { index, code ->
            if (index < elements.size && code != NORMAL) {
                val element = elements[index]
                val label = getResistanceLabel(code)
                if (label.isNotEmpty()) {
                    val icon = getElementIcon(element)
                    result.add("$icon $element: $label")
                }
            }
        }

        return result.joinToString(" | ")
    }

    /**
     * Get Unicode icon for element
     */
    private fun getElementIcon(element: String): String {
        return when (element) {
            "Fire" -> "🔥"
            "Ice" -> "❄️"
            "Elec" -> "⚡"
            "Wind" -> "💨"
            "Light", "Bless" -> "✨"
            "Dark", "Curse" -> "🌑"
            "Phys", "Physical" -> "👊"
            "Slash" -> "⚔️"
            "Strike" -> "✊"
            "Pierce" -> "🗡️"
            "Gun" -> "🔫"
            "Psy" -> "🧠"
            "Nuke", "Nuclear" -> "☢️"
            "Almighty" -> "💫"
            else -> "•"
        }
    }
}
