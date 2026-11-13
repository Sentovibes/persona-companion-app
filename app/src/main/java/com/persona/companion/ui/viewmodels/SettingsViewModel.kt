package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class LegacyImagesStatus(
    val exists: Boolean = false,
    val fileCount: Int = 0,
    val storageSizeBytes: Long = 0L
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)

    private val _settings = MutableStateFlow(prefs.getSettings())
    val settings = _settings.asStateFlow()

    private val _legacyImagesStatus = MutableStateFlow(LegacyImagesStatus())
    val legacyImagesStatus = _legacyImagesStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    init {
        refreshLegacyImagesStatus()
    }

    fun refreshLegacyImagesStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _legacyImagesStatus.value = scanLegacyImagesDir()
        }
    }

    fun clearLegacyImages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imagesDir = File(getApplication<Application>().filesDir, "images")
                if (imagesDir.exists()) {
                    imagesDir.deleteRecursively()
                }
                val legacyPrefs = getApplication<Application>().getSharedPreferences("image_download_prefs", 0)
                legacyPrefs.edit().clear().apply()
                _legacyImagesStatus.value = scanLegacyImagesDir()
                _statusMessage.value = "Legacy image cache cleared."
            } catch (e: Exception) {
                _statusMessage.value = e.message ?: "Failed to clear legacy image cache"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun toggleDlc(show: Boolean) {
        prefs.setShowDlc(show)
        _settings.update { it.copy(showDlc = show) }
    }

    fun toggleEpisodeAigis(show: Boolean) {
        prefs.setShowEpisodeAigis(show)
        _settings.update { it.copy(showEpisodeAigis = show) }
    }

    private fun scanLegacyImagesDir(): LegacyImagesStatus {
        val imagesDir = File(getApplication<Application>().filesDir, "images")
        if (!imagesDir.exists() || !imagesDir.isDirectory) {
            return LegacyImagesStatus()
        }
        var count = 0
        var totalBytes = 0L
        imagesDir.walkTopDown().filter { it.isFile }.forEach { file ->
            count++
            totalBytes += file.length()
        }
        return LegacyImagesStatus(
            exists = count > 0,
            fileCount = count,
            storageSizeBytes = totalBytes
        )
    }
}
