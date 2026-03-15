package com.persona.companion.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.AppPreferences
import com.persona.companion.data.ImageDownloadManager
import com.persona.companion.data.imagedownload.DownloadPhase
import com.persona.companion.data.imagedownload.DownloadProgress
import com.persona.companion.data.imagedownload.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)

    private val _settings = MutableStateFlow(prefs.getSettings())
    val settings = _settings.asStateFlow()

    private val _downloadStatus = MutableStateFlow(DownloadStatus(false, null, null, 0L))
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        refreshDownloadStatus()
    }

    fun refreshDownloadStatus() {
        viewModelScope.launch {
            val status = withContext(Dispatchers.IO) {
                ImageDownloadManager.getDownloadStatus(getApplication())
            }
            _downloadStatus.value = status
        }
    }

    fun importFromUri(context: Context, uri: Uri) {
        _isDownloading.value = true
        _errorMessage.value = null
        _downloadProgress.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val result = ImageDownloadManager.importFromUri(context, uri) { progress ->
                _downloadProgress.value = progress
                if (progress.phase == DownloadPhase.COMPLETE) {
                    _isDownloading.value = false
                    val status = ImageDownloadManager.getDownloadStatus(context)
                    _downloadStatus.value = status
                } else if (progress.phase == DownloadPhase.ERROR) {
                    _isDownloading.value = false
                    _errorMessage.value = progress.error
                }
            }
            result.onFailure { error ->
                _isDownloading.value = false
                _errorMessage.value = error.message ?: "Import failed"
            }
        }
    }

    fun deleteImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = ImageDownloadManager.deleteImages(getApplication())
            result.onSuccess {
                val status = ImageDownloadManager.getDownloadStatus(getApplication())
                _downloadStatus.value = status
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to delete images"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun toggleDlc(show: Boolean) {
        prefs.setShowDlc(show)
        _settings.update { it.copy(showDlc = show) }
    }

    fun toggleEpisodeAigis(show: Boolean) {
        prefs.setShowEpisodeAigis(show)
        _settings.update { it.copy(showEpisodeAigis = show) }
    }
}
