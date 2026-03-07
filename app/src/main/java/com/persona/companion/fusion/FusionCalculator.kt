package com.persona.companion.fusion

import com.persona.companion.models.Persona

data class FusionRecipe(
    val personas: List<Persona>  // Changed to support 2+ personas
)

class FusionCalculator(
    private val fusionRecipes: Map<String, List<List<String>>>,
    private val specialFusions: Map<String, List<List<String>>>,
    private val allPersonas: List<Persona>
) {
    
    // Create a map for quick persona lookup by name
    private val personaMap = allPersonas.associateBy { it.name }
    
    /**
     * Get all fusion recipes for a target persona
     * Prioritizes special fusions if they exist
     */
    fun calculateFusionsFor(targetPersona: Persona): List<FusionRecipe> {
        val recipes = mutableListOf<FusionRecipe>()
        
        // Check for special fusions first
        val specialRecipeData = specialFusions[targetPersona.name]
        if (specialRecipeData != null && specialRecipeData.isNotEmpty()) {
            // This persona has special fusions - use ONLY those
            for (recipe in specialRecipeData) {
                val personas = recipe.mapNotNull { personaMap[it] }
                if (personas.size == recipe.size) {
                    recipes.add(FusionRecipe(personas))
                }
            }
            return recipes
        }
        
        // No special fusions - use normal 2-persona fusions
        val recipeData = fusionRecipes[targetPersona.name] ?: return recipes
        
        // Convert recipe data to FusionRecipe objects
        for (recipe in recipeData) {
            if (recipe.size != 2) continue
            
            val persona1 = personaMap[recipe[0]]
            val persona2 = personaMap[recipe[1]]
            
            if (persona1 != null && persona2 != null) {
                recipes.add(FusionRecipe(listOf(persona1, persona2)))
            }
        }
        
        return recipes
    }
}
