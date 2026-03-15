package com.persona.companion.fusion

import com.persona.companion.models.FusionChart
import com.persona.companion.models.Persona

data class FusionRecipe(
    val personas: List<Persona>
)

/**
 * Fission-based fusion calculator matching the megaten-fusion-tool algorithm.
 *
 * Cross-arcana: splitWithDiffRace (smt-nonelem-fissions.ts)
 *   lvlModifier = 0.5
 *   minResultLvl = 2*(prevTargetLvl - 0.5) = 2*prevTargetLvl - 1
 *   maxResultLvl = 2*(targetLvl - 0.5)     = 2*targetLvl - 1
 *   condition: minLvlB < lvlB <= maxLvlB  (raceA != raceB only)
 *
 * Same-arcana: splitWithSameRace (per-nonelem-fissions.ts)
 *   lvlModifier = 1
 *   minResultLvl = 2*(targetLvl - 1)
 *   maxResultLvl = 2*(nextTargetLvl - 1)
 *   condition: minResultLvl <= lvl1+lvl2 < maxResultLvl
 *
 * Fission table built via loadFissionTableJson logic:
 *   for each cell table[r][c] = raceR: fissionTable[raceR][raceA].add(raceB)
 *   (one direction only — no symmetric reverse)
 */
class FusionCalculator(
    private val chart: FusionChart,
    private val allPersonas: List<Persona>,
    private val specialFusions: Map<String, List<List<String>>>,
    private val isTriangular: Boolean = false
) {
    private val personaMap: Map<String, Persona> = allPersonas.associateBy { it.name }

    // Only fusable personas grouped by arcana, sorted by level ascending
    // Element demons (empty special list) are excluded from ingredient pool
    private val elementDemonNames: Set<String> = specialFusions
        .filter { (_, v) -> v.isEmpty() }
        .keys

    private val byArcana: Map<String, List<Persona>> = allPersonas
        .filter { it.fusion != "party" && it.fusion != "accident" && it.fusion != "special" && it.name !in elementDemonNames }
        .groupBy { it.arcana ?: "" }
        .mapValues { (_, list) -> list.sortedBy { it.level ?: 0 } }

    // Fission table: resultArcana -> { arcanaA -> [arcanaB, ...] }
    // Mirrors megaten tool's loadFissionTableJson — one direction per cell only
    private val fissionTable: Map<String, Map<String, List<String>>> by lazy { buildFissionTable() }

    private fun buildFissionTable(): Map<String, Map<String, List<String>>> {
        val races = chart.races
        val table = chart.table
        val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()

        if (isTriangular) {
            // Triangular chart: row i has i+1 columns, raceB = races[c]
            for (idxA in races.indices) {
                val raceA = races[idxA]
                val row = table.getOrNull(idxA) ?: continue
                for (c in row.indices) {
                    if (c == idxA) continue // skip diagonal
                    val raceR = row.getOrNull(c) ?: continue
                    if (raceR.isBlank() || raceR == "-") continue
                    val raceB = races.getOrNull(c) ?: continue
                    result.getOrPut(raceR) { mutableMapOf() }
                        .getOrPut(raceA) { mutableListOf() }
                        .also { if (!it.contains(raceB)) it.add(raceB) }
                }
            }
        } else {
            // Square chart: upper triangle only (j >= i, skip diagonal)
            for (idxA in races.indices) {
                val raceA = races[idxA]
                val row = table.getOrNull(idxA) ?: continue
                for (idxB in idxA until races.size) {
                    if (idxB == idxA) continue // skip diagonal
                    val raceB = races.getOrNull(idxB) ?: continue
                    val raceR = row.getOrNull(idxB) ?: continue
                    if (raceR.isBlank() || raceR == "-") continue
                    result.getOrPut(raceR) { mutableMapOf() }
                        .getOrPut(raceA) { mutableListOf() }
                        .also { if (!it.contains(raceB)) it.add(raceB) }
                }
            }
        }
        return result
    }

    /** Returns all 2-persona combos that produce [target]. */
    fun calculateFusionsFor(target: Persona): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()

        // Special fusions — return early
        val special = specialFusions[target.name]
        if (!special.isNullOrEmpty()) {
            for (ingredients in special) {
                val personas = ingredients.mapNotNull { personaMap[it] }
                if (personas.size == ingredients.size) {
                    recipes.add(FusionRecipe(personas))
                }
            }
            return recipes
        }

        val targetArcana = target.arcana ?: return recipes
        val targetLvl = target.level ?: return recipes

        // resultLvls excludes special-recipe personas (mirrors getResultDemonLvls)
        val specialNames = specialFusions.keys.toSet()
        val resultLvls = (byArcana[targetArcana] ?: emptyList())
            .filter { it.name !in specialNames }
            .map { it.level ?: 0 }
            .sorted()

        val targetLvlIndex = resultLvls.indexOf(targetLvl)
        if (targetLvlIndex < 0) return recipes

        // ── Same-arcana (per-nonelem-fissions.ts: splitWithSameRace) ──
        // minResultLvl = 2*(targetLvl - 1),  maxResultLvl = 2*(nextLvl - 1)
        val sameMinLvl = 2 * (targetLvl - 1)
        val sameMaxLvl = if (targetLvlIndex + 1 < resultLvls.size)
            2 * (resultLvls[targetLvlIndex + 1] - 1) else 200
        val sameNextLvl = if (targetLvlIndex + 2 < resultLvls.size)
            2 * (resultLvls[targetLvlIndex + 2] - 1) else 200

        // ingLvls = all arcana levels except target (mirrors getIngredientDemonLvls)
        val ingLvls = (byArcana[targetArcana] ?: emptyList())
            .map { it.level ?: 0 }
            .filter { it != targetLvl }
            .sorted()

        // Extra recipe: ingLvlM x ingLvl2 (per-nonelem-fissions.ts lines 22-28)
        val ingLvlM = sameMaxLvl / 2 + 1  // maxResultLvl/2 + lvlModifier(1)
        for (lvl2 in ingLvls) {
            if (ingLvlM < lvl2 && ingLvlM + lvl2 < sameNextLvl) {
                val pM = byArcana[targetArcana]?.firstOrNull { it.level == ingLvlM } ?: continue
                val p2 = byArcana[targetArcana]?.firstOrNull { it.level == lvl2 } ?: continue
                recipes.add(FusionRecipe(listOf(pM, p2)))
            }
        }

        // Normal same-arcana pairs
        for (i in ingLvls.indices) {
            val lvl1 = ingLvls[i]
            for (j in i + 1 until ingLvls.size) {
                val lvl2 = ingLvls[j]
                val sum = lvl1 + lvl2
                if (sum in sameMinLvl until sameMaxLvl) {
                    val p1 = byArcana[targetArcana]?.firstOrNull { it.level == lvl1 } ?: continue
                    val p2 = byArcana[targetArcana]?.firstOrNull { it.level == lvl2 } ?: continue
                    recipes.add(FusionRecipe(listOf(p1, p2)))
                }
            }
        }

        // ── Cross-arcana (smt-nonelem-fissions.ts: splitWithDiffRace) ──
        // prevTargetLvl = resultLvls[targetLvlIndex - 1]
        // minResultLvl = 2*(prevTargetLvl - 0.5)  (or 0)
        // maxResultLvl = 2*(targetLvl - 0.5)  only if next result exists, else 200
        val prevTargetLvl = if (targetLvlIndex > 0) resultLvls[targetLvlIndex - 1] else null
        val nextTargetLvl = if (targetLvlIndex + 1 < resultLvls.size) resultLvls[targetLvlIndex + 1] else null
        val crossMinLvl = if (prevTargetLvl != null) 2 * prevTargetLvl - 1 else 0
        val crossMaxLvl = if (nextTargetLvl != null) 2 * targetLvl - 1 else 200

        val fissions = fissionTable[targetArcana] ?: emptyMap()
        val seen = mutableSetOf<Pair<String, String>>()

        for ((raceA, raceBsList) in fissions) {
            val lvlsA = (byArcana[raceA] ?: emptyList()).map { it.level ?: 0 }
            for (lvlA in lvlsA) {
                val minLvlB = crossMinLvl - lvlA
                val maxLvlB = crossMaxLvl - lvlA
                for (raceB in raceBsList) {
                    if (raceA == raceB) continue // same-arcana handled above
                    val lvlsB = (byArcana[raceB] ?: emptyList()).map { it.level ?: 0 }
                    for (lvlB in lvlsB) {
                        if (lvlB > minLvlB && lvlB <= maxLvlB && (raceA != raceB || lvlA < lvlB)) {
                            val nameA = byArcana[raceA]?.firstOrNull { it.level == lvlA }?.name ?: continue
                            val nameB = byArcana[raceB]?.firstOrNull { it.level == lvlB }?.name ?: continue
                            val key = if (nameA <= nameB) Pair(nameA, nameB) else Pair(nameB, nameA)
                            if (seen.add(key)) {
                                val p1 = personaMap[nameA] ?: continue
                                val p2 = personaMap[nameB] ?: continue
                                recipes.add(FusionRecipe(listOf(p1, p2)))
                            }
                        }
                    }
                }
            }
        }

        return recipes
    }
}
