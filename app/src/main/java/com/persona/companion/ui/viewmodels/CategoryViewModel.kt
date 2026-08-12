package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.database.AppDatabase
import com.persona.companion.data.repositories.RequestRepository
import kotlinx.coroutines.flow.*

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = RequestRepository(application, database.requestDao())

    private val _currentGameId = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val completedCount: StateFlow<Int> = _currentGameId.flatMapLatest { gameId ->
        if (gameId == null) flowOf(0)
        else repository.getCompletedCount(gameId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val totalCount: StateFlow<Int> = _currentGameId.flatMapLatest { gameId ->
        if (gameId == null) flowOf(0)
        else repository.getTotalCount(gameId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun loadGame(gameId: String) {
        _currentGameId.value = gameId
    }
}
