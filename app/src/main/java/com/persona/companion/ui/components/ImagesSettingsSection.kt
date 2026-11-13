package com.persona.companion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.SettingsViewModel

@Composable
fun ImagesSettingsSection(vm: SettingsViewModel = viewModel()) {
    val legacyStatus by vm.legacyImagesStatus.collectAsState()
    val statusMessage by vm.statusMessage.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
                imageVector = Icons.Default.Collections,
                contentDescription = null,
                tint = AccentBrass,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bundled Artwork Catalog",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "All Persona, Shadow, and Boss artworks are built in for offline use (WebP catalog).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        if (legacyStatus.exists) {
            val mb = ((legacyStatus.storageSizeBytes / 1_000_000L).coerceAtLeast(1L))
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = HairlineSoft)
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Legacy Image Pack Detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${legacyStatus.fileCount} extracted files (${mb} MB) from an older install can be safely removed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear")
                }
            }
        }

        if (!statusMessage.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentBrass,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { vm.clearStatusMessage() }) {
                    Text("Dismiss")
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        val mb = ((legacyStatus.storageSizeBytes / 1_000_000L).coerceAtLeast(1L))
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Clear Legacy Image Pack") },
            text = { Text("Remove ${legacyStatus.fileCount} old extracted image files (${mb} MB)? Built-in artwork will continue working offline.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    vm.clearLegacyImages()
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}
