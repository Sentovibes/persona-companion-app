package com.persona.companion.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.BuildConfig
import com.persona.companion.data.ImageDownloadManager
import com.persona.companion.data.imagedownload.DownloadPhase
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.SettingsViewModel

@Composable
fun ImagesSettingsSection(vm: SettingsViewModel = viewModel()) {
    val context = LocalContext.current

    val downloadStatus by vm.downloadStatus.collectAsState()
    val downloadProgress by vm.downloadProgress.collectAsState()
    val isImporting by vm.isDownloading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // File picker for importing images.zip
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vm.importFromUri(context, uri)
        }
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
                    isImporting && downloadProgress != null -> {
                        Text(
                            text = when (downloadProgress!!.phase) {
                                DownloadPhase.CHECKING_STORAGE -> "Checking storage..."
                                DownloadPhase.DOWNLOADING -> {
                                    val mb = downloadProgress!!.bytesDownloaded / 1_000_000
                                    val total = downloadProgress!!.totalBytes / 1_000_000
                                    "Importing: $mb MB / $total MB"
                                }
                                DownloadPhase.EXTRACTING ->
                                    "Extracting: ${downloadProgress!!.filesExtracted} / ${downloadProgress!!.totalFiles} files"
                                DownloadPhase.COMPLETE -> "Import complete!"
                                DownloadPhase.ERROR -> downloadProgress!!.error ?: "Error occurred"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (downloadProgress!!.phase == DownloadPhase.ERROR)
                                MaterialTheme.colorScheme.error else TextSecondary
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

        // Progress bar while importing
        if (isImporting && downloadProgress != null) {
            Spacer(Modifier.height(12.dp))
            when (downloadProgress!!.phase) {
                DownloadPhase.DOWNLOADING -> {
                    LinearProgressIndicator(
                        progress = {
                            if (downloadProgress!!.totalBytes > 0)
                                downloadProgress!!.bytesDownloaded.toFloat() / downloadProgress!!.totalBytes.toFloat()
                            else 0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    val pct = if (downloadProgress!!.totalBytes > 0)
                        (downloadProgress!!.bytesDownloaded * 100 / downloadProgress!!.totalBytes).toInt() else 0
                    Text("$pct%", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                }
                DownloadPhase.EXTRACTING -> {
                    LinearProgressIndicator(
                        progress = {
                            if (downloadProgress!!.totalFiles > 0)
                                downloadProgress!!.filesExtracted.toFloat() / downloadProgress!!.totalFiles.toFloat()
                            else 0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DownloadPhase.CHECKING_STORAGE -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                else -> {}
            }
        }

        // Action buttons
        if (!isImporting) {
            Spacer(Modifier.height(12.dp))

            if (!downloadStatus.isDownloaded) {
                // Step 1: download link
                Text(
                    text = buildAnnotatedString {
                        append("Step 1: ")
                        withStyle(SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )) {
                            append("Click here to download images.zip")
                        }
                        append(" in your browser")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .then(Modifier.clickableNoRipple {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.IMAGES_CDN_URL))
                            context.startActivity(intent)
                        })
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Step 2: Once downloaded, tap Import below and select images.zip",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (downloadStatus.isDownloaded) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }

                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (downloadStatus.isDownloaded) "Re-import" else "Import")
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Images") },
            text = { Text("Delete all downloaded images? This will free up ${downloadStatus.storageSize / 1_000_000} MB.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    vm.deleteImages()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

// Helper — clickable without ripple for the link row
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = MutableInteractionSource(),
            onClick = onClick
        )
    )
