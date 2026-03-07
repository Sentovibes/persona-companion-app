package com.persona.companion.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.fusion.FusionCalculator
import com.persona.companion.fusion.FusionRecipe
import com.persona.companion.models.FusionChart
import com.persona.companion.models.Persona
import com.persona.companion.utils.JsonLoader
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FusionState(
    val personas: List<Persona> = emptyList(),
    val selectedPersona: Persona? = null,
    val fusionRecipes: List<FusionRecipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FusionViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(FusionState())
    val state: StateFlow<FusionState> = _state.asStateFlow()
    
    private var fusionCalculator: FusionCalculator? = null
    
    fun loadData(context: Context, seriesId: String, gameId: String, dataPath: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Load personas
                val personas = JsonLoader.loadPersonas(context, dataPath)
                
                // Load fusion recipes
                val recipePath = when (gameId) {
                    "p3r" -> "data/fusion-recipes/p3r-recipes.json"
                    "p4g" -> "data/fusion-recipes/p4g-recipes.json"
                    "p5" -> "data/fusion-recipes/p5-recipes.json"
                    "p5r" -> "data/fusion-recipes/p5r-recipes.json"
                    else -> null
                }
                
                if (recipePath == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Fusion not available for this game"
                    )
                    return@launch
                }
                
                val recipeJson = context.assets.open(recipePath).bufferedReader().use { it.readText() }
                val recipeData = Gson().fromJson<Map<String, Any>>(recipeJson, object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type)
                
                // Convert recipe data to the format we need
                val fusionRecipes = mutableMapOf<String, List<List<String>>>()
                for ((personaName, data) in recipeData) {
                    if (data is Map<*, *>) {
                        val recipes = data["recipes"]
                        if (recipes is List<*>) {
                            @Suppress("UNCHECKED_CAST")
                            fusionRecipes[personaName] = recipes as List<List<String>>
                        }
                    }
                }
                
                fusionCalculator = FusionCalculator(fusionRecipes, personas)
                
                _state.value = _state.value.copy(
                    personas = personas.sortedBy { it.name },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.message}"
                )
            }
        }
    }
    
    fun selectPersona(persona: Persona) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val calculator = fusionCalculator
            if (calculator == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Fusion calculator not initialized"
                )
                return@launch
            }
            
            val recipes = calculator.calculateFusionsFor(persona)
            
            _state.value = _state.value.copy(
                selectedPersona = persona,
                fusionRecipes = recipes,
                isLoading = false
            )
        }
    }
    
    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedPersona = null,
            fusionRecipes = emptyList()
        )
    }
}
