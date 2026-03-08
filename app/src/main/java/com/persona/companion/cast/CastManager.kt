package com.persona.companion.cast

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages the Cast server lifecycle
 */
object CastManager {
    private const val TAG = "CastManager"
    private var server: CastServer? = null
    private var isRunning = false
    
    var onServerStarted: ((String) -> Unit)? = null
    var onServerStopped: (() -> Unit)? = null
    var onClientConnected: (() -> Unit)? = null
    var onClientDisconnected: (() -> Unit)? = null
    
    /**
     * Start the cast server
     */
    fun startServer(context: Context) {
        if (isRunning) {
            Log.w(TAG, "Server already running")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = CastServer(context).apply {
                    onClientConnected = this@CastManager.onClientConnected
                    onClientDisconnected = this@CastManager.onClientDisconnected
                    start()
                }
                
                isRunning = true
                val url = server?.getConnectionUrl() ?: ""
                Log.i(TAG, "Cast server started at: $url")
                
                CoroutineScope(Dispatchers.Main).launch {
                    onServerStarted?.invoke(url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server", e)
                isRunning = false
            }
        }
    }
    
    /**
     * Stop the cast server
     */
    fun stopServer() {
        if (!isRunning) {
            Log.w(TAG, "Server not running")
            return
        }
        
        try {
            server?.stop()
            server = null
            isRunning = false
            Log.i(TAG, "Cast server stopped")
            onServerStopped?.invoke()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop server", e)
        }
    }
    
    /**
     * Check if server is running
     */
    fun isServerRunning(): Boolean = isRunning
    
    /**
     * Get connection URL
     */
    fun getConnectionUrl(): String? = server?.getConnectionUrl()
    
    /**
     * Broadcast enemy data to all connected TVs
     */
    fun broadcastEnemy(enemy: Any) {
        server?.broadcastEnemy(enemy)
    }
    
    /**
     * Broadcast persona data to all connected TVs
     */
    fun broadcastPersona(persona: Any) {
        server?.broadcastPersona(persona)
    }
}
