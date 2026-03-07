package com.persona.companion.fusion

import com.persona.companion.models.FusionChart
import com.persona.companion.models.Persona

data class FusionRecipe(
    val persona1: Persona,
    val persona2: Persona,
    val resultLevel: Int
)

class FusionCalculator(
    private val fusionChart: FusionChart,
    private val allPersonas: List<Persona>
) {
    
    /**
     * Calculate all possible fusion recipes that result in the target persona.
     * This is backwards fusion (fission).
     */
    fun calculateFusionsFor(targetPersona: Persona): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()
        
        val targetArcana = targetPersona.arcana ?: return recipes
        val targetLevel = targetPersona.level ?: return recipes
        
        // Get all personas of the same arcana
        val sameArcanaPersonas = allPersonas.filter { 
            it.arcana == targetArcana && it.name != targetPersona.name 
        }
        
        // Get all personas sorted by level for each arcana
        val personasByArcana = allPersonas
            .filter { it.arcana != null && it.level != null }
            .groupBy { it.arcana!! }
            .mapValues { (_, personas) -> personas.sortedBy { it.level } }
        
        // Get result levels for target arcana
        val resultLevels = sameArcanaPersonas
            .mapNotNull { it.level }
            .distinct()
            .sorted()
        
        val targetLevelIndex = resultLevels.indexOf(targetLevel)
        if (targetLevelIndex < 0) return recipes
        
        // Calculate level ranges
        val lvlModifier = 1
        val minResultLevel = 2 * (targetLevel - lvlModifier)
        val maxResultLevel = resultLevels.getOrNull(targetLevelIndex + 1)
            ?.let { 2 * (it - lvlModifier) } ?: 200
        
        // Same-arcana fusion
        recipes.addAll(calculateSameArcanaFusions(
            targetArcana,
            targetPersona.name,
            minResultLevel,
            maxResultLevel,
            sameArcanaPersonas
        ))
        
        // Cross-arcana fusion
        recipes.addAll(calculateCrossArcanaFusions(
            targetArcana,
            targetLevel,
            personasByArcana
        ))
        
        return recipes.distinctBy { "${it.persona1.name}|${it.persona2.name}" }
    }
    
    private fun calculateSameArcanaFusions(
        targetArcana: String,
        targetName: String,
        minLevel: Int,
        maxLevel: Int,
        sameArcanaPersonas: List<Persona>
    ): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()
        val personas = sameArcanaPersonas.filter { it.name != targetName }
        
        for (i in personas.indices) {
            val p1 = personas[i]
            val lvl1 = p1.level ?: continue
            
            for (j in i + 1 until personas.size) {
                val p2 = personas[j]
                val lvl2 = p2.level ?: continue
                
                val sumLevel = lvl1 + lvl2
                if (sumLevel in minLevel until maxLevel) {
                    recipes.add(FusionRecipe(p1, p2, sumLevel / 2))
                }
            }
        }
        
        return recipes
    }
    
    private fun calculateCrossArcanaFusions(
        targetArcana: String,
        targetLevel: Int,
        personasByArcana: Map<String, List<Persona>>
    ): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()
        
        // Try all arcana combinations
        val arcanas = personasByArcana.keys.toList()
        
        for (arcana1 in arcanas) {
            for (arcana2 in arcanas) {
                // Check if this combination produces target arcana
                val resultArcana = fusionChart.getFusionResult(arcana1, arcana2)
                if (resultArcana != targetArcana) continue
                
                val personas1 = personasByArcana[arcana1] ?: continue
                val personas2 = personasByArcana[arcana2] ?: continue
                
                // Try all persona combinations
                for (p1 in personas1) {
                    for (p2 in personas2) {
                        if (p1.name == p2.name) continue
                        
                        val avgLevel = calculateAverageLevel(p1.level ?: 0, p2.level ?: 0)
                        val resultPersona = findClosestPersona(targetArcana, avgLevel, personasByArcana)
                        
                        if (resultPersona?.name == findPersonaByName(targetArcana, targetLevel, personasByArcana)?.name) {
                            recipes.add(FusionRecipe(p1, p2, avgLevel))
                        }
                    }
                }
            }
        }
        
        return recipes
    }
    
    private fun calculateAverageLevel(level1: Int, level2: Int): Int {
        return (level1 + level2) / 2 + 1
    }
    
    private fun findClosestPersona(arcana: String, targetLevel: Int, personasByArcana: Map<String, List<Persona>>): Persona? {
        val personas = personasByArcana[arcana] ?: return null
        return personas.filter { (it.level ?: 0) <= targetLevel }.maxByOrNull { it.level ?: 0 }
    }
    
    private fun findPersonaByName(arcana: String, level: Int, personasByArcana: Map<String, List<Persona>>): Persona? {
        val personas = personasByArcana[arcana] ?: return null
        return personas.find { it.level == level }
    }
}
