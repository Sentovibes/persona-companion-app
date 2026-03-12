package com.persona.companion.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.persona.companion.BuildConfig
import com.persona.companion.data.UserPreferences
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.components.ImagesSettingsSection
import com.persona.companion.ui.viewmodels.SettingsViewModel
import com.persona.companion.utils.UpdateChecker
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    vm: SettingsViewModel = viewModel()
) {
    val settings by vm.settings.collectAsState()
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<com.persona.companion.utils.UpdateInfo?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Persona 3 Portable",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                P3PProtagonistSelector(
                    selectedProtagonist = userPrefs.getP3PProtagonist(),
                    onProtagonistChange = { protagonist ->
                        userPrefs.setP3PProtagonist(protagonist)
                    }
                )
            }

            item {
                Text(
                    text = "Content Filters",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingToggle(
                    title = "Show DLC Personas",
                    description = "Include downloadable content personas in lists",
                    checked = settings.showDlc,
                    onCheckedChange = vm::toggleDlc
                )
            }

            item {
                SettingToggle(
                    title = "Show Episode Aigis Personas",
                    description = "Include personas from Episode Aigis (P3 Reload) and The Answer (P3 FES)",
                    checked = settings.showEpisodeAigis,
                    onCheckedChange = vm::toggleEpisodeAigis
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Note: Changes will apply when you reload the persona list",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "HD Images",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                ImagesSettingsSection()
            }
            
            // Debug info button (only in debug builds)
            if (BuildConfig.ENABLE_DEBUG_FEATURES) {
                item {
                    var debugInfo by remember { mutableStateOf<String?>(null) }
                    var showDebugDialog by remember { mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .clickable {
                                val debugFile = File(context.getExternalFilesDir(null), "import_debug.txt")
                                debugInfo = if (debugFile.exists()) {
                                    debugFile.readText()
                                } else {
                                    "No debug file found. Import images first."
                                }
                                showDebugDialog = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Import Debug Info",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "View detailed import diagnostics",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    if (showDebugDialog) {
                        AlertDialog(
                            onDismissRequest = { showDebugDialog = false },
                            title = { Text("Import Debug Info") },
                            text = {
                                androidx.compose.foundation.lazy.LazyColumn {
                                    item {
                                        Text(
                                            text = debugInfo ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showDebugDialog = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "App Info",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Refresh,
                    title = "Check for Updates",
                    description = if (isCheckingUpdate) "Checking..." else updateMessage ?: "Tap to check for new versions",
                    onClick = {
                        isCheckingUpdate = true
                        updateMessage = null
                        scope.launch {
                            val result = UpdateChecker.checkForUpdates()
                            isCheckingUpdate = false
                            result.onSuccess { info ->
                                updateInfo = info
                                if (info.isUpdateAvailable) {
                                    showUpdateDialog = true
                                } else {
                                    updateMessage = "You're on the latest version (${info.latestVersion})"
                                }
                            }.onFailure {
                                updateMessage = "Failed to check for updates"
                            }
                        }
                    }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    description = "Version ${BuildConfig.VERSION_NAME} • Made with ❤️ for Persona fans",
                    onClick = {}
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/sentovibes"))
                            context.startActivity(intent)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Support Development",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Buy me a coffee on Ko-fi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "☕",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "This is a fan project with no affiliation to Atlus or SEGA",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Available") },
            text = {
                Column {
                    Text("Version ${updateInfo!!.latestVersion} is now available!")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "You'll be redirected to download the new version.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo!!.downloadUrl))
                        context.startActivity(intent)
                        showUpdateDialog = false
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = TextSecondary
            )
        )
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun P3PProtagonistSelector(
    selectedProtagonist: UserPreferences.P3PProtagonist,
    onProtagonistChange: (UserPreferences.P3PProtagonist) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(16.dp)
    ) {
        Text(
            text = "P3P Protagonist",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Choose which protagonist's Social Links to display",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Male MC button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedProtagonist == UserPreferences.P3PProtagonist.MALE)
                            TextPrimary.copy(alpha = 0.2f)
                        else
                            Background
                    )
                    .clickable { onProtagonistChange(UserPreferences.P3PProtagonist.MALE) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Male MC",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedProtagonist == UserPreferences.P3PProtagonist.MALE)
                        TextPrimary
                    else
                        TextSecondary
                )
            }

            // FeMC button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedProtagonist == UserPreferences.P3PProtagonist.FEMC)
                            TextPrimary.copy(alpha = 0.2f)
                        else
                            Background
                    )
                    .clickable { onProtagonistChange(UserPreferences.P3PProtagonist.FEMC) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FeMC",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedProtagonist == UserPreferences.P3PProtagonist.FEMC)
                        TextPrimary
                    else
                        TextSecondary
                )
            }
        }
    }
}


