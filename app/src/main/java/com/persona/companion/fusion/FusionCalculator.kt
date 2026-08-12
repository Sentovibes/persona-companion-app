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

    fun isSpecialFusion(name: String): Boolean {
        return specialFusions.containsKey(name)
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

    fun getResultArcana(arcanaA: String, arcanaB: String): String? {
        if (arcanaA == arcanaB) return arcanaA
        val races = chart.races
        val idxA = races.indexOf(arcanaA)
        val idxB = races.indexOf(arcanaB)
        if (idxA < 0 || idxB < 0) return null

        return if (isTriangular) {
            val r = maxOf(idxA, idxB)
            val c = minOf(idxA, idxB)
            chart.table.getOrNull(r)?.getOrNull(c)
        } else {
            val r = minOf(idxA, idxB)
            val c = maxOf(idxA, idxB)
            chart.table.getOrNull(r)?.getOrNull(c)
        }
    }

    fun fuseTriangle(p1: Persona, p2: Persona, p3: Persona): Persona? {
        if (p1.name == p2.name || p2.name == p3.name || p1.name == p3.name) return null

        val sorted = listOf(p1, p2, p3).sortedWith(compareBy({ it.level ?: 0 }, { it.name }))
        val low1 = sorted[0]
        val low2 = sorted[1]
        val high = sorted[2]

        val arcana1 = low1.arcana ?: return null
        val arcana2 = low2.arcana ?: return null
        val arcana3 = high.arcana ?: return null

        val tempArc = getResultArcana(arcana1, arcana2) ?: return null
        if (tempArc.isBlank() || tempArc == "-") return null

        val finalArc = getResultArcana(tempArc, arcana3) ?: return null
        if (finalArc.isBlank() || finalArc == "-") return null

        val targetList = byArcana[finalArc] ?: return null
        val calcLvl = ((low1.level ?: 0) + (low2.level ?: 0) + (high.level ?: 0)) / 3 + 5

        val specialNames = specialFusions.keys.toSet()
        val candidates = targetList.filter {
            it.name != low1.name && it.name != low2.name && it.name != high.name &&
            it.name !in specialNames && it.fusion != "party" && it.fusion != "accident" && it.fusion != "special" &&
            (it.level ?: 0) >= calcLvl
        }

        return candidates.firstOrNull() ?: targetList.filter {
            it.name != low1.name && it.name != low2.name && it.name != high.name &&
            it.name !in specialNames && it.fusion != "party" && it.fusion != "accident" && it.fusion != "special"
        }.lastOrNull()
    }

    fun calculateTripleFusionsFor(target: Persona): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()
        val targetArcana = target.arcana ?: return recipes
        val targetLvl = target.level ?: return recipes

        val specialNames = specialFusions.keys.toSet()
        if (target.name in specialNames || target.fusion == "party" || target.fusion == "accident" || target.fusion == "special") {
            return recipes // Specials don't come from normal triangle fusions
        }

        val targetList = (byArcana[targetArcana] ?: emptyList())
            .filter { it.name !in specialNames }
            .sortedBy { it.level ?: 0 }
        val targetIdx = targetList.indexOfFirst { it.name == target.name }
        if (targetIdx < 0) return recipes

        val minCalcLvl = if (targetIdx > 0) (targetList[targetIdx - 1].level ?: 0) + 1 else 0
        val maxCalcLvl = if (targetIdx < targetList.size - 1) targetLvl else 200

        val minSum = 3 * (minCalcLvl - 5)
        val maxSum = 3 * (maxCalcLvl - 4) - 1

        val seen = mutableSetOf<Triple<String, String, String>>()

        for (i in allPersonas.indices) {
            val p1 = allPersonas[i]
            val lvl1 = p1.level ?: continue
            val arc1 = p1.arcana ?: continue
            if (p1.name in specialNames || p1.fusion == "party" || p1.fusion == "accident" || p1.fusion == "special" || p1.name in elementDemonNames) continue

            for (j in i + 1 until allPersonas.size) {
                val p2 = allPersonas[j]
                val lvl2 = p2.level ?: continue
                val arc2 = p2.arcana ?: continue
                if (p2.name in specialNames || p2.fusion == "party" || p2.fusion == "accident" || p2.fusion == "special" || p2.name in elementDemonNames) continue

                val tempArc = getResultArcana(arc1, arc2) ?: continue
                if (tempArc.isBlank() || tempArc == "-") continue

                for (k in j + 1 until allPersonas.size) {
                    val p3 = allPersonas[k]
                    val lvl3 = p3.level ?: continue
                    val arc3 = p3.arcana ?: continue
                    if (p3.name in specialNames || p3.fusion == "party" || p3.fusion == "accident" || p3.fusion == "special" || p3.name in elementDemonNames) continue

                    val sum = lvl1 + lvl2 + lvl3
                    if (sum < minSum || sum > maxSum) continue

                    val finalArc = getResultArcana(tempArc, arc3) ?: continue
                    if (finalArc != targetArcana) continue

                    val result = fuseTriangle(p1, p2, p3)
                    if (result?.name == target.name) {
                        val key = Triple(p1.name, p2.name, p3.name)
                        if (seen.add(key)) {
                            recipes.add(FusionRecipe(listOf(p1, p2, p3)))
                        }
                    }
                }
            }
        }

        return recipes
    }

    fun estimatePersonaCost(level: Int): Int {
        return 27 * level * level + 120 * level + 2000
    }

    fun findSpecialFusion(ingredients: List<Persona>): Persona? {
        val names = ingredients.map { it.name }.toSet()
        for ((resultName, recipes) in specialFusions) {
            for (recipe in recipes) {
                if (recipe.size == ingredients.size && recipe.toSet() == names) {
                    return personaMap[resultName]
                }
            }
        }
        return null
    }

    fun fuse(ingredients: List<Persona>): Persona? {
        if (ingredients.isEmpty()) return null

        val special = findSpecialFusion(ingredients)
        if (special != null) return special

        if (ingredients.size == 2) {
            val p1 = ingredients[0]
            val p2 = ingredients[1]
            val arcana1 = p1.arcana ?: return null
            val arcana2 = p2.arcana ?: return null

            if (arcana1 == arcana2) {
                val list = byArcana[arcana1] ?: return null
                val avgLvl = ((p1.level ?: 0) + (p2.level ?: 0)) / 2.0
                val candidates = list.filter { it.name != p1.name && it.name != p2.name && (it.level ?: 0) < avgLvl }
                return candidates.lastOrNull()
            } else {
                val resArcana = getResultArcana(arcana1, arcana2) ?: return null
                if (resArcana.isBlank() || resArcana == "-") return null
                val list = byArcana[resArcana] ?: return null
                val avgLvl = ((p1.level ?: 0) + (p2.level ?: 0)) / 2.0 + 1.0
                val candidates = list.filter { it.name != p1.name && it.name != p2.name && (it.level ?: 0) >= avgLvl }
                return candidates.firstOrNull() ?: list.lastOrNull()
            }
        } else if (ingredients.size == 3) {
            return fuseTriangle(ingredients[0], ingredients[1], ingredients[2])
        }

        return null
    }
}
