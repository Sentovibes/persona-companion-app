package com.persona.companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.persona.companion.data.AppPreferences
import com.persona.companion.navigation.NavGraph
import com.persona.companion.ui.theme.PersonaCompanionTheme
import com.persona.companion.utils.UpdateChecker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonaCompanionTheme {
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
            }
        }
    }
}
