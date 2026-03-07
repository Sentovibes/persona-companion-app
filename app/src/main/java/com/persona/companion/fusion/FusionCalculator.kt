package com.persona.companion.fusion

import com.persona.companion.models.Persona

data class FusionRecipe(
    val persona1: Persona,
    val persona2: Persona
)

class FusionCalculator(
    private val fusionRecipes: Map<String, List<List<String>>>,
    private val allPersonas: List<Persona>
) {
    
    // Create a map for quick persona lookup by name
    private val personaMap = allPersonas.associateBy { it.name }
    
    /**
     * Get all fusion recipes for a target persona using pre-calculated data
     */
    fun calculateFusionsFor(targetPersona: Persona): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()
        
        // Get the pre-calculated recipes for this persona
        val recipeData = fusionRecipes[targetPersona.name] ?: return recipes
        
        // Convert recipe data to FusionRecipe objects
        for (recipe in recipeData) {
            if (recipe.size != 2) continue
            
            val persona1 = personaMap[recipe[0]]
            val persona2 = personaMap[recipe[1]]
            
            if (persona1 != null && persona2 != null) {
                recipes.add(FusionRecipe(persona1, persona2))
            }
        }
        
        return recipes
    }
}
