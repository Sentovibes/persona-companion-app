package com.persona.companion.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.AppPreferences
import com.persona.companion.data.PersonaRepository
import com.persona.companion.data.UserPreferences
import com.persona.companion.models.Persona
import com.persona.companion.models.PersonaFilters
import com.persona.companion.utils.FilterUtils
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
    val sortBy: SortOption = SortOption.ARCANA,
    val filters: PersonaFilters = PersonaFilters(),
    val favorites: Set<String> = emptySet(),
    val seriesId: String = "",
    val gameId: String = ""
)

class PersonaListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PersonaRepository(application)
    private val prefs = AppPreferences(application)
    private val userPrefs = UserPreferences(application)

    private val _state = MutableStateFlow(PersonaListState())
    val state = _state.asStateFlow()

    fun load(dataPath: String, aigisDataPath: String? = null, seriesId: String = "", gameId: String = "") {
        Log.d(TAG, "load() called with dataPath: $dataPath, aigisDataPath: $aigisDataPath")
        _state.update { it.copy(
            isLoading = true, 
            query = "", 
            personas = emptyList(), 
            filtered = emptyList(), 
            errorMessage = null, 
            debugInfo = "Loading from: $dataPath",
            favorites = userPrefs.getFavoritePersonas(),
            seriesId = seriesId,
            gameId = gameId
        ) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting to load personas from: $dataPath")
                val all = repo.getPersonas(dataPath).toMutableList()
                Log.d(TAG, "Loaded ${all.size} personas from base path")
                
                // Load Aigis personas if path provided and setting enabled
                val settings = prefs.getSettings()
                if (aigisDataPath != null && settings.showEpisodeAigis) {
                    try {
                        val aigisPersonas = repo.getPersonas(aigisDataPath)
                        Log.d(TAG, "Loaded ${aigisPersonas.size} Aigis personas")
                        all.addAll(aigisPersonas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading Aigis personas", e)
                    }
                }
                
                // Apply filters based on settings
                val filtered = all.filter { persona ->
                    val includeDlc = settings.showDlc || persona.isDlc != true
                    val includeAigis = settings.showEpisodeAigis || persona.episodeAigis != true
                    includeDlc && includeAigis
                }
                
                val debug = "Path: $dataPath\nAigis Path: $aigisDataPath\nLoaded: ${all.size} personas\nFiltered: ${filtered.size} personas\nFirst 3: ${filtered.take(3).map { it.name }}"
                _state.update { it.copy(
                    personas = filtered, 
                    filtered = applyFiltersAndSort(filtered, it.filters, it.favorites, it.query, it.seriesId, it.gameId), 
                    isLoading = false, 
                    debugInfo = debug
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading personas", e)
                _state.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}", debugInfo = "Path: $dataPath\nError: ${e.stackTraceToString()}") }
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { current ->
            current.copy(
                query = query, 
                filtered = applyFiltersAndSort(current.personas, current.filters, current.favorites, query, current.seriesId, current.gameId)
            )
        }
    }

    fun setSortOption(sortBy: SortOption) {
        _state.update { current ->
            current.copy(sortBy = sortBy, filtered = applyFiltersAndSort(current.personas, current.filters, current.favorites, current.query, current.seriesId, current.gameId))
        }
    }
    
    fun setFilters(filters: PersonaFilters) {
        _state.update { current ->
            current.copy(
                filters = filters,
                filtered = applyFiltersAndSort(current.personas, filters, current.favorites, current.query, current.seriesId, current.gameId)
            )
        }
    }
    
    fun toggleFavorite(seriesId: String, gameId: String, persona: Persona) {
        val personaId = FilterUtils.getPersonaId(seriesId, gameId, persona)
        if (userPrefs.isFavoritePersona(personaId)) {
            userPrefs.removeFavoritePersona(personaId)
        } else {
            userPrefs.addFavoritePersona(personaId)
        }
        _state.update { it.copy(favorites = userPrefs.getFavoritePersonas()) }
    }

    private fun applyFiltersAndSort(
        personas: List<Persona>, 
        filters: PersonaFilters,
        favorites: Set<String>,
        query: String,
        seriesId: String,
        gameId: String
    ): List<Persona> {
        // First apply search query
        var filtered = if (query.isBlank()) {
            personas
        } else {
            val lower = query.lowercase()
            personas.filter {
                it.name.lowercase().contains(lower) || 
                (it.arcana?.lowercase()?.contains(lower) == true)
            }
        }
        
        // Then apply filters and sort
        filtered = FilterUtils.filterAndSortPersonas(filtered, filters, favorites, seriesId, gameId)
        
        return filtered
    }
}

