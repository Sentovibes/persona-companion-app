package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.PersonaRepository
import com.persona.companion.models.Persona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonaListState(
    val personas: List<Persona> = emptyList(),
    val filtered: List<Persona> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true
)

class PersonaListViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PersonaRepository(application)

    private val _state = MutableStateFlow(PersonaListState())
    val state = _state.asStateFlow()

    fun load(dataPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repo.getPersonas(dataPath)
            _state.update { it.copy(personas = all, filtered = all, isLoading = false) }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { current ->
            val filtered = if (query.isBlank()) {
                current.personas
            } else {
                val lower = query.lowercase()
                current.personas.filter {
                    it.name.lowercase().contains(lower) || it.arcana.lowercase().contains(lower)
                }
            }
            current.copy(query = query, filtered = filtered)
        }
    }
}
