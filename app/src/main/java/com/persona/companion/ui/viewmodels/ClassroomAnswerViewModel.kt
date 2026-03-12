package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.ClassroomAnswerLoader
import com.persona.companion.models.ClassroomData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClassroomAnswerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _classroomData = MutableStateFlow<ClassroomData?>(null)
    val classroomData: StateFlow<ClassroomData?> = _classroomData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load classroom answers for a specific game
     */
    fun loadClassroomAnswers(gameId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            
            try {
                val data = ClassroomAnswerLoader.loadClassroomAnswers(getApplication(), gameId)
                _classroomData.value = data
                
                if (data == null) {
                    _error.value = "Classroom answers not available for this game"
                }
            } catch (e: Exception) {
                _error.value = "Error loading classroom answers: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
