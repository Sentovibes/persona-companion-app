package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.UserPreferences
import com.persona.companion.models.Enemy
import com.persona.companion.models.EnemyFilters
import com.persona.companion.utils.FilterUtils
import com.persona.companion.utils.JsonLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EnemyListState(
    val enemies: List<Enemy> = emptyList(),
    val filtered: List<Enemy> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filters: EnemyFilters = EnemyFilters(),
    val favorites: Set<String> = emptySet(),
    val gameId: String = "",
    val seriesId: String = ""
)

class EnemyListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userPrefs = UserPreferences(application)
    private val _state = MutableStateFlow(EnemyListState())
    val state: StateFlow<EnemyListState> = _state.asStateFlow()
    
    fun loadEnemies(enemyPath: String?, aigisEnemyPath: String? = null, gameId: String, seriesId: String = "") {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, favorites = userPrefs.getFavoriteEnemies(), gameId = gameId, seriesId = seriesId) }
            
            try {
                val enemies = mutableListOf<Enemy>()

                // Load base enemies
                if (enemyPath != null) {
                    enemies.addAll(JsonLoader.loadEnemies(getApplication(), enemyPath))
                }

                // Load Aigis enemies if path provided and setting enabled
                val settings = com.persona.companion.data.AppPreferences(getApplication()).getSettings()
                if (aigisEnemyPath != null && settings.showEpisodeAigis) {
                    try {
                        enemies.addAll(JsonLoader.loadEnemies(getApplication(), aigisEnemyPath))
                    } catch (e: Exception) {
                        // Aigis enemies optional, don't fail if missing
                    }
                }

                // Hide enemies flagged episodeAigis (inline in the base file, e.g. P3R) when the setting is off
                if (!settings.showEpisodeAigis) {
                    enemies.removeAll { it.episodeAigis == true }
                }

                _state.update { current ->
                    current.copy(
                        enemies = enemies,
                        filtered = applyFiltersAndSort(enemies, current.filters, current.favorites, gameId, current.seriesId),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load enemies: ${e.message}"
                ) }
            }
        }
    }
    
    fun setFilters(filters: EnemyFilters) {
        _state.update { current ->
            current.copy(
                filters = filters,
                filtered = applyFiltersAndSort(current.enemies, filters, current.favorites, current.gameId, current.seriesId)
            )
        }
    }
    
    fun toggleFavorite(seriesId: String, gameId: String, enemy: Enemy) {
        val enemyId = FilterUtils.getEnemyId(seriesId, gameId, enemy)
        if (userPrefs.isFavoriteEnemy(enemyId)) {
            userPrefs.removeFavoriteEnemy(enemyId)
        } else {
            userPrefs.addFavoriteEnemy(enemyId)
        }
        _state.update { it.copy(favorites = userPrefs.getFavoriteEnemies()) }
    }
    
    private fun applyFiltersAndSort(
        enemies: List<Enemy>,
        filters: EnemyFilters,
        favorites: Set<String>,
        gameId: String,
        seriesId: String
    ): List<Enemy> {
        val elements = getElementsForGame(gameId)
        return FilterUtils.filterAndSortEnemies(enemies, filters, favorites, elements, seriesId, gameId)
    }
    
    private fun getElementsForGame(gameId: String): List<String> =
        com.persona.companion.utils.ResistanceUtils.getElementNames(gameId)
}
