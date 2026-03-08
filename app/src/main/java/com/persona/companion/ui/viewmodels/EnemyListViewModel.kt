package com.persona.companion.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.models.Enemy
import com.persona.companion.utils.JsonLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EnemyListState(
    val enemies: List<Enemy> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EnemyListViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(EnemyListState())
    val state: StateFlow<EnemyListState> = _state.asStateFlow()
    
    fun loadEnemies(context: Context, enemyPath: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val enemies = if (enemyPath != null) {
                    JsonLoader.loadEnemies(context, enemyPath)
                } else {
                    emptyList()
                }
                
                _state.value = _state.value.copy(
                    enemies = enemies,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load enemies: ${e.message}"
                )
            }
        }
    }
}
