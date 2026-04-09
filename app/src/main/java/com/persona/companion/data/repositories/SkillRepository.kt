package com.persona.companion.data.repositories

import android.content.Context
import com.persona.companion.models.Skill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SkillRepository(private val context: Context) {

    // Element code to human-readable name mapping (from the megaten fusion tool)
    private val elementMap = mapOf(
        "sla" to "Slash", "str" to "Strike", "pie" to "Pierce",
        "fir" to "Fire", "ice" to "Ice", "win" to "Wind", "ele" to "Elec",
        "lig" to "Light", "dar" to "Dark", "alm" to "Almighty",
        "rec" to "Recovery", "sup" to "Support", "pas" to "Passive",
        "ail" to "Ailment", "spe" to "Special",
        "phy" to "Phys", "gun" to "Gun", "psy" to "Psy", "nuk" to "Nuclear",
        "for" to "Force", "ear" to "Earth", "wat" to "Water"
    )

    suspend fun getSkills(skillPath: String): List<Skill> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open(skillPath).bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val skills = mutableListOf<Skill>()

            val keys = root.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val entry = root.getJSONObject(id)

                val a = entry.getJSONArray("a") // [name, element, target]
                val b = entry.getJSONArray("b") // [rank, cost, power, minHits, maxHits, accuracy, crit, extra]

                val name = a.getString(0)
                val elemCode = a.getString(1)
                val target = a.getString(2)
                val element = elementMap[elemCode] ?: elemCode

                val rank = b.getInt(0)
                val costRaw = b.getInt(1)
                // Cost > 1000 is SP cost (subtract 1000), otherwise HP cost
                val cost = if (costRaw > 1000) costRaw - 1000 else costRaw
                val costType = if (costRaw > 1000) "SP" else if (costRaw > 0) "HP" else ""

                // Build effect string from 'c' array if present
                val effect = buildEffectString(entry, target, costRaw)

                skills.add(
                    Skill(
                        name = name,
                        element = element,
                        cost = cost,
                        costType = costType,
                        effect = effect,
                        target = target,
                        rank = rank
                    )
                )
            }

            // Sort by element, then by name; filter out enemy-only skills (no rank)
            skills.filter { it.rank > 0 || it.name in listOf("Attack") || it.element in listOf("Passive") }
                .sortedWith(compareBy({ it.element }, { it.name }))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun buildEffectString(entry: JSONObject, target: String, costRaw: Int): String {
        val b = entry.getJSONArray("b")
        val power = b.getInt(2)

        val parts = mutableListOf<String>()

        // Add power info
        val powerLabel = when {
            power >= 800 -> "Severe"
            power >= 500 -> "Heavy"
            power >= 300 -> "Heavy"
            power >= 150 -> "Medium"
            power >= 50 -> "Light"
            power > 0 -> "Minuscule"
            else -> ""
        }

        // Check if it has an ailment/effect from c array
        if (entry.has("c")) {
            val c = entry.getJSONArray("c")
            val ailment = if (c.length() > 0) c.getString(0) else "-"
            if (ailment != "-") {
                if (powerLabel.isNotEmpty()) {
                    parts.add("$powerLabel damage to $target")
                    parts.add(ailment)
                } else {
                    parts.add("$ailment to $target")
                }
            } else if (powerLabel.isNotEmpty()) {
                parts.add("$powerLabel damage to $target")
            }
        } else if (powerLabel.isNotEmpty()) {
            parts.add("$powerLabel damage to $target")
        }

        return parts.joinToString(". ").ifEmpty { target }
    }
}
