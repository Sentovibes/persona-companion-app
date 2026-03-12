package com.persona.companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.persona.companion.data.AppPreferences
import com.persona.companion.debug.DebugErrorHandler
import com.persona.companion.debug.DebugLogger
import com.persona.companion.debug.DebugOverlay
import com.persona.companion.navigation.NavGraph
import com.persona.companion.ui.theme.PersonaCompanionTheme
import com.persona.companion.utils.UpdateChecker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize debug error handler
        if (BuildConfig.ENABLE_DEBUG_FEATURES) {
            DebugErrorHandler.install(application)
            DebugLogger.i("MainActivity", "Debug mode enabled - version ${BuildConfig.VERSION_NAME}")
        }
        
        enableEdgeToEdge()
        setContent {
            val userPrefs = remember { com.persona.companion.data.UserPreferences(this) }
            val isDarkMode by remember { mutableStateOf(userPrefs.isDarkMode()) }
            
            PersonaCompanionTheme(darkTheme = isDarkMode) {
                var showDebugOverlay by remember { mutableStateOf(false) }
                var lastError by remember { mutableStateOf<Throwable?>(null) }
                
                // Listen for errors in debug mode
                DisposableEffect(Unit) {
                    val callback: (Throwable) -> Unit = { throwable ->
                        lastError = throwable
                        showDebugOverlay = true
                    }
                    if (BuildConfig.ENABLE_DEBUG_FEATURES) {
                        DebugErrorHandler.addErrorCallback(callback)
                    }
                    onDispose {
                        if (BuildConfig.ENABLE_DEBUG_FEATURES) {
                            DebugErrorHandler.removeErrorCallback(callback)
                        }
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        
                        var showUpdateDialog by remember { mutableStateOf(false) }
                        var updateInfo by remember { mutableStateOf<com.persona.companion.utils.UpdateInfo?>(null) }
                        val scope = rememberCoroutineScope()
                        
                        LaunchedEffect(Unit) {
                            val prefs = AppPreferences(this@MainActivity)
                            if (prefs.shouldCheckForUpdates()) {
                                scope.launch {
                                    val result = UpdateChecker.checkForUpdates()
                                    result.onSuccess { info ->
                                        prefs.setLastUpdateCheck(System.currentTimeMillis())
                                        if (info.isUpdateAvailable) {
                                            updateInfo = info
                                            showUpdateDialog = true
                                        }
                                    }
                                }
                            }
                        }
                        
                        NavGraph(navController = navController)
                        
                        if (showUpdateDialog && updateInfo != null) {
                            AlertDialog(
                                onDismissRequest = { showUpdateDialog = false },
                                title = { Text("Update Available") },
                                text = { Text("Version ${updateInfo!!.latestVersion} is now available! Would you like to download it?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo!!.downloadUrl))
                                            startActivity(intent)
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
                    
                    // Debug floating button (only in debug builds)
                    if (BuildConfig.ENABLE_DEBUG_FEATURES) {
                        FloatingActionButton(
                            onClick = { showDebugOverlay = !showDebugOverlay },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Icon(Icons.Default.BugReport, "Debug Console")
                        }
                    }
                    
                    // Debug overlay
                    DebugOverlay(
                        visible = showDebugOverlay,
                        onDismiss = { showDebugOverlay = false }
                    )
                    
                    // Show error toast in debug mode
                    lastError?.let { error ->
                        LaunchedEffect(error) {
                            DebugLogger.e("MainActivity", "Caught error: ${error.message}", error)
                        }
                    }
                }
            }
        }
    }
}
