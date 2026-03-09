package com.persona.companion.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.persona.companion.cast.CastManager
import com.persona.companion.utils.QRCodeGenerator

/**
 * Cast button with connection dialog
 */
@Composable
fun CastButton() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionUrl by remember { mutableStateOf<String?>(null) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Set up callbacks
    LaunchedEffect(Unit) {
        CastManager.onServerStarted = { url ->
            if (url.startsWith("Error:")) {
                errorMessage = url
                connectionUrl = null
                qrCodeBitmap = null
            } else {
                connectionUrl = url
                qrCodeBitmap = try {
                    QRCodeGenerator.generateQRCode(url, 512)
                } catch (e: Exception) {
                    null
                }
                errorMessage = null
            }
        }
        
        CastManager.onClientConnected = {
            isConnected = true
        }
        
        CastManager.onClientDisconnected = {
            isConnected = false
        }
    }
    
    IconButton(onClick = { showDialog = true }) {
        Icon(
            imageVector = if (isConnected) Icons.Default.CastConnected else Icons.Default.Cast,
            contentDescription = "Cast",
            tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
    
    if (showDialog) {
        CastDialog(
            isConnected = isConnected,
            connectionUrl = connectionUrl,
            qrCodeBitmap = qrCodeBitmap,
            errorMessage = errorMessage,
            onDismiss = { showDialog = false },
            onStartCasting = {
                try {
                    errorMessage = null
                    // Start server directly - no service needed
                    CastManager.startServer(context)
                } catch (e: Exception) {
                    errorMessage = "Failed to start: ${e.message}"
                }
            },
            onStopCasting = {
                try {
                    CastManager.stopServer()
                    isConnected = false
                    connectionUrl = null
                    qrCodeBitmap = null
                    errorMessage = null
                } catch (e: Exception) {
                    errorMessage = "Failed to stop: ${e.message}"
                }
            }
        )
    }
}

@Composable
private fun CastDialog(
    isConnected: Boolean,
    connectionUrl: String?,
    qrCodeBitmap: Bitmap?,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onStartCasting: () -> Unit,
    onStopCasting: () -> Unit
) {
    val isServerRunning = CastManager.isServerRunning()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cast to TV",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show error message if any
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (!isServerRunning) {
                    // Not started yet
                    Text(
                        text = "Start casting to display content on your TV or browser",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onStartCasting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Cast, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Casting")
                    }
                } else {
                    // Server running
                    if (isConnected) {
                        Text(
                            text = "✓ TV Connected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Waiting for TV to connect...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show URL prominently
                    connectionUrl?.let { url ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "On your TV browser, go to:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = url,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show QR code for phone scanning
                    qrCodeBitmap?.let { bitmap ->
                        Text(
                            text = "Or scan with another phone:",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.size(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onStopCasting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Stop Casting")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
