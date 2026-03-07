package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.persona.companion.data.AppPreferences
import com.persona.companion.data.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = AppPreferences(application)
    
    private val _settings = MutableStateFlow(prefs.getSettings())
    val settings = _settings.asStateFlow()
    
    fun toggleDlc(show: Boolean) {
        prefs.setShowDlc(show)
        _settings.update { it.copy(showDlc = show) }
    }
    
    fun toggleEpisodeAigis(show: Boolean) {
        prefs.setShowEpisodeAigis(show)
        _settings.update { it.copy(showEpisodeAigis = show) }
    }
}
