package com.persona.companion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.persona.companion.BuildConfig
import com.persona.companion.data.ImageDownloadManager
import com.persona.companion.data.imagedownload.DownloadPhase
import com.persona.companion.data.imagedownload.DownloadProgress
import com.persona.companion.data.imagedownload.DownloadStatus
import com.persona.companion.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ImagesSettingsSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var downloadStatus by remember { mutableStateOf(ImageDownloadManager.getDownloadStatus(context)) }
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var showMeteredWarning by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // File picker for importing images.zip
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isDownloading = true
            errorMessage = null
            scope.launch {
                val result = ImageDownloadManager.importFromUri(context, uri) { progress ->
                    downloadProgress = progress
                    if (progress.phase == DownloadPhase.COMPLETE) {
                        isDownloading = false
                        downloadStatus = ImageDownloadManager.getDownloadStatus(context)
                        errorMessage = null
                    } else if (progress.phase == DownloadPhase.ERROR) {
                        isDownloading = false
                        errorMessage = progress.error
                    }
                }
                
                // Handle final result
                result.onSuccess {
                    isDownloading = false
                    downloadStatus = ImageDownloadManager.getDownloadStatus(context)
                    errorMessage = null
                }.onFailure { error ->
                    isDownloading = false
                    errorMessage = error.message ?: "Import failed"
                }
            }
        }
    }
    
    // Refresh status when returning to screen
    LaunchedEffect(Unit) {
        downloadStatus = ImageDownloadManager.getDownloadStatus(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HD Images",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                
                when {
                    isDownloading && downloadProgress != null -> {
                        Text(
                            text = when (downloadProgress!!.phase) {
                                DownloadPhase.CHECKING_STORAGE -> "Checking storage..."
                                DownloadPhase.DOWNLOADING -> {
                                    val mbDownloaded = downloadProgress!!.bytesDownloaded / 1_000_000
                                    val mbTotal = downloadProgress!!.totalBytes / 1_000_000
                                    "Downloading: $mbDownloaded MB / $mbTotal MB"
                                }
                                DownloadPhase.EXTRACTING -> {
                                    "Extracting: ${downloadProgress!!.filesExtracted} / ${downloadProgress!!.totalFiles} files"
                                }
                                DownloadPhase.COMPLETE -> "Download complete!"
                                DownloadPhase.ERROR -> downloadProgress!!.error ?: "Error occurred"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (downloadProgress!!.phase == DownloadPhase.ERROR) 
                                MaterialTheme.colorScheme.error 
                            else 
                                TextSecondary
                        )
                    }
                    downloadStatus.isDownloaded -> {
                        val sizeMB = downloadStatus.storageSize / 1_000_000
                        Text(
                            text = "Downloaded • $sizeMB MB • Version ${downloadStatus.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    else -> {
                        val estimatedSizeMB = BuildConfig.IMAGES_ZIP_SIZE / 1_000_000
                        Text(
                            text = "Not downloaded • ~$estimatedSizeMB MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                // Show error message if present
                if (errorMessage != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Progress indicator
        if (isDownloading && downloadProgress != null) {
            Spacer(Modifier.height(12.dp))
            
            when (downloadProgress!!.phase) {
                DownloadPhase.DOWNLOADING -> {
                    LinearProgressIndicator(
                        progress = { 
                            if (downloadProgress!!.totalBytes > 0) {
                                downloadProgress!!.bytesDownloaded.toFloat() / downloadProgress!!.totalBytes.toFloat()
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    val percentage = if (downloadProgress!!.totalBytes > 0) {
                        (downloadProgress!!.bytesDownloaded * 100 / downloadProgress!!.totalBytes).toInt()
                    } else {
                        0
                    }
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDisabled
                    )
                }
                DownloadPhase.EXTRACTING -> {
                    LinearProgressIndicator(
                        progress = {
                            if (downloadProgress!!.totalFiles > 0) {
                                downloadProgress!!.filesExtracted.toFloat() / downloadProgress!!.totalFiles.toFloat()
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DownloadPhase.CHECKING_STORAGE -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {}
            }
        }
        
        // Action buttons
        if (!isDownloading) {
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (downloadStatus.isDownloaded) {
                    // Delete button
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                    
                    // Import button (to re-import/update)
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Import")
                    }
                } else {
                    // Download button
                    Button(
                        onClick = {
                            // Check if on metered connection
                            if (ImageDownloadManager.isMeteredConnection(context)) {
                                showMeteredWarning = true
                            } else {
                                startDownload(
                                    context = context,
                                    scope = scope,
                                    onProgressUpdate = { progress ->
                                        downloadProgress = progress
                                        if (progress.phase == DownloadPhase.COMPLETE) {
                                            isDownloading = false
                                            downloadStatus = ImageDownloadManager.getDownloadStatus(context)
                                            errorMessage = null
                                        } else if (progress.phase == DownloadPhase.ERROR) {
                                            isDownloading = false
                                            errorMessage = progress.error
                                        }
                                    },
                                    onStart = { isDownloading = true }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Download")
                    }
                    
                    // Import button
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Import")
                    }
                }
                
                // Retry button if error
                if (errorMessage != null) {
                    Button(
                        onClick = {
                            errorMessage = null
                            startDownload(
                                context = context,
                                scope = scope,
                                onProgressUpdate = { progress ->
                                    downloadProgress = progress
                                    if (progress.phase == DownloadPhase.COMPLETE) {
                                        isDownloading = false
                                        downloadStatus = ImageDownloadManager.getDownloadStatus(context)
                                        errorMessage = null
                                    } else if (progress.phase == DownloadPhase.ERROR) {
                                        isDownloading = false
                                        errorMessage = progress.error
                                    }
                                },
                                onStart = { isDownloading = true }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
    
    // Metered connection warning dialog
    if (showMeteredWarning) {
        AlertDialog(
            onDismissRequest = { showMeteredWarning = false },
            title = { Text("Metered Connection") },
            text = {
                Text("You are on a metered connection. This will download approximately ${BuildConfig.IMAGES_ZIP_SIZE / 1_000_000} MB. Continue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMeteredWarning = false
                        startDownload(
                            context = context,
                            scope = scope,
                            onProgressUpdate = { progress ->
                                downloadProgress = progress
                                if (progress.phase == DownloadPhase.COMPLETE) {
                                    isDownloading = false
                                    downloadStatus = ImageDownloadManager.getDownloadStatus(context)
                                    errorMessage = null
                                } else if (progress.phase == DownloadPhase.ERROR) {
                                    isDownloading = false
                                    errorMessage = progress.error
                                }
                            },
                            onStart = { isDownloading = true }
                        )
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMeteredWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Images") },
            text = {
                Text("Are you sure you want to delete all downloaded images? This will free up ${downloadStatus.storageSize / 1_000_000} MB.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        scope.launch {
                            val result = ImageDownloadManager.deleteImages(context)
                            result.onSuccess { sizeFreed ->
                                downloadStatus = ImageDownloadManager.getDownloadStatus(context)
                                errorMessage = null
                            }.onFailure { error ->
                                errorMessage = error.message ?: "Failed to delete images"
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun startDownload(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onProgressUpdate: (DownloadProgress) -> Unit,
    onStart: () -> Unit
) {
    onStart()
    scope.launch {
        ImageDownloadManager.startDownload(context) { progress ->
            onProgressUpdate(progress)
        }
    }
}
