package com.persona.companion.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.AppPreferences
import com.persona.companion.data.PersonaRepository
import com.persona.companion.models.Persona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PersonaListViewModel"

enum class SortOption {
    ARCANA, LEVEL, NAME
}

data class PersonaListState(
    val personas: List<Persona> = emptyList(),
    val filtered: List<Persona> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val debugInfo: String = "",
    val sortBy: SortOption = SortOption.ARCANA
)

class PersonaListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PersonaRepository(application)
    private val prefs = AppPreferences(application)

    private val _state = MutableStateFlow(PersonaListState())
    val state = _state.asStateFlow()

    fun load(dataPath: String) {
        Log.d(TAG, "load() called with dataPath: $dataPath")
        _state.update { it.copy(isLoading = true, query = "", personas = emptyList(), filtered = emptyList(), errorMessage = null, debugInfo = "Loading from: $dataPath") }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting to load personas from: $dataPath")
                val all = repo.getPersonas(dataPath)
                Log.d(TAG, "Loaded ${all.size} personas")
                
                // Apply filters based on settings
                val settings = prefs.getSettings()
                val filtered = all.filter { persona ->
                    val includeDlc = settings.showDlc || persona.isDlc != true
                    val includeAigis = settings.showEpisodeAigis || persona.episodeAigis != true
                    includeDlc && includeAigis
                }
                
                val debug = "Path: $dataPath\nLoaded: ${all.size} personas\nFiltered: ${filtered.size} personas\nFirst 3: ${filtered.take(3).map { it.name }}"
                _state.update { it.copy(personas = filtered, filtered = applySorting(filtered, it.sortBy), isLoading = false, debugInfo = debug) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading personas", e)
                _state.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}", debugInfo = "Path: $dataPath\nError: ${e.stackTraceToString()}") }
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { current ->
            val filtered = if (query.isBlank()) {
                current.personas
            } else {
                val lower = query.lowercase()
                current.personas.filter {
                    it.name.lowercase().contains(lower) || 
                    (it.arcana?.lowercase()?.contains(lower) == true)
                }
            }
            Log.d(TAG, "Query: '$query', filtered: ${filtered.size} personas")
            current.copy(query = query, filtered = applySorting(filtered, current.sortBy))
        }
    }

    fun setSortOption(sortBy: SortOption) {
        _state.update { current ->
            current.copy(sortBy = sortBy, filtered = applySorting(current.filtered, sortBy))
        }
    }

    private fun applySorting(personas: List<Persona>, sortBy: SortOption): List<Persona> {
        return when (sortBy) {
            SortOption.ARCANA -> personas.sortedWith(compareBy({ it.arcana ?: "Unknown" }, { it.level ?: 0 }))
            SortOption.LEVEL -> personas.sortedBy { it.level ?: 0 }
            SortOption.NAME -> personas.sortedBy { it.name }
        }
    }
}
