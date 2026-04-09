package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.repositories.ItemRepository
import com.persona.companion.data.PersonaRepository
import com.persona.companion.models.Item
import com.persona.companion.models.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.persona.companion.data.AppPreferences
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val personaRepository = PersonaRepository(application)
    private val prefs = AppPreferences(application)

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    
    // Combine items with settings to filter on the fly
    val items: StateFlow<List<Item>> = combine(
        _items,
        prefs.getSettingsFlow()
    ) { items, settings ->
        items.filter { settings.showEpisodeAigis || it.episodeAigis != true }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadItems(gameId: String, itemPath: String?) {
        if (itemPath == null) {
            _items.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _items.value = repository.getItems(gameId, itemPath)
            _isLoading.value = false
        }
    }

    fun getItemSources(personaPath: String?, itemName: String): List<Persona> {
        if (personaPath == null) return emptyList()
        return personaRepository.getPersonasByItemization(personaPath, itemName)
    }
}
