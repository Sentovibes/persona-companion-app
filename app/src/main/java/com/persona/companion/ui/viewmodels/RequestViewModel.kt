package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.database.AppDatabase
import com.persona.companion.data.repositories.RequestRepository
import com.persona.companion.models.RequestEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RequestViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = RequestRepository(application, database.requestDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentGameId = MutableStateFlow<String?>(null)

    val requests: StateFlow<List<RequestEntity>> = combine(
        _currentGameId.flatMapLatest { gameId ->
            if (gameId == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else repository.getRequests(gameId)
        },
        _searchQuery
    ) { requests, query ->
        if (query.isBlank()) {
            requests
        } else {
            requests.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.remarks?.contains(query, ignoreCase = true) == true ||
                it.reward.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRequests(gameId: String, requestPath: String?) {
        _currentGameId.value = gameId
        if (requestPath == null) return

        viewModelScope.launch {
            _isLoading.value = true
            repository.syncRequestsIfNeeded(gameId, requestPath)
            _isLoading.value = false
        }
    }

    fun toggleRequestCompletion(name: String, gameId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(name, gameId, completed)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
