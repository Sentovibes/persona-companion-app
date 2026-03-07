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
    
    fun loadData(context: Context, seriesId: String, dataPath: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Load personas
                val personas = JsonLoader.loadPersonas(context, dataPath)
                
                // Load fusion chart
                val chartPath = when (seriesId) {
                    "p3" -> "data/fusion-charts/p3-fusion-chart.json"
                    "p4" -> "data/fusion-charts/p4-fusion-chart.json"
                    "p5" -> "data/fusion-charts/p5-fusion-chart.json"
                    else -> null
                }
                
                if (chartPath == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Fusion not available for this game"
                    )
                    return@launch
                }
                
                val chartJson = context.assets.open(chartPath).bufferedReader().use { it.readText() }
                val fusionChart = Gson().fromJson(chartJson, FusionChart::class.java)
                
                fusionCalculator = FusionCalculator(fusionChart, personas)
                
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
