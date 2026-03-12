package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.SocialLinkLoader
import com.persona.companion.models.SocialLink
import com.persona.companion.models.SocialLinksData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialLinkViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _socialLinksData = MutableStateFlow<SocialLinksData?>(null)
    val socialLinksData: StateFlow<SocialLinksData?> = _socialLinksData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load Social Links for a specific game
     */
    fun loadSocialLinks(gameId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            
            try {
                val data = SocialLinkLoader.loadSocialLinks(getApplication(), gameId)
                _socialLinksData.value = data
                
                if (data == null) {
                    _error.value = "Social Links data not available for this game"
                }
            } catch (e: Exception) {
                _error.value = "Error loading Social Links: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get a specific Social Link by arcana name
     */
    fun getSocialLinkByArcana(arcana: String): SocialLink? {
        return _socialLinksData.value?.socialLinks?.find { 
            it.arcana.equals(arcana, ignoreCase = true) 
        }
    }
}
