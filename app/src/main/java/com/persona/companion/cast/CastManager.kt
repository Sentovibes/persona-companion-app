package com.persona.companion.cast

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import fi.iki.elonen.NanoHTTPD
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
    private var wifiLock: WifiManager.WifiLock? = null
    
    var onServerStarted: ((String) -> Unit)? = null
    var onServerStopped: (() -> Unit)? = null
    var onClientConnected: (() -> Unit)? = null
    var onClientDisconnected: (() -> Unit)? = null
    
    /**
     * Start the cast server
     */
    fun startServer(context: Context) {
        Log.d(TAG, "startServer called, isRunning=$isRunning")
        
        if (isRunning) {
            Log.w(TAG, "Server already running")
            onServerStarted?.invoke(server?.getConnectionUrl() ?: "")
            return
        }
        
        // Acquire WiFi lock to keep WiFi active
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "PersonaCompanion::CastWifiLock").apply {
            acquire()
            Log.d(TAG, "WiFi lock acquired")
        }
        
        // Start on IO thread to avoid blocking UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Creating CastServer instance...")
                val newServer = CastServer(context, 8080)
                
                Log.d(TAG, "Setting up callbacks...")
                newServer.onClientConnected = this@CastManager.onClientConnected
                newServer.onClientDisconnected = this@CastManager.onClientDisconnected
                
                Log.d(TAG, "Starting server...")
                newServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                
                server = newServer
                isRunning = true
                
                val url = newServer.getConnectionUrl()
                Log.i(TAG, "Cast server started successfully at: $url")
                
                // Notify on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    onServerStarted?.invoke(url)
                }
            } catch (e: java.net.BindException) {
                Log.e(TAG, "Port 8080 already in use", e)
                isRunning = false
                releaseWifiLock()
                CoroutineScope(Dispatchers.Main).launch {
                    onServerStarted?.invoke("Error: Port 8080 is already in use")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server", e)
                Log.e(TAG, "Exception type: ${e.javaClass.name}")
                Log.e(TAG, "Exception message: ${e.message}")
                Log.e(TAG, "Stack trace:")
                e.printStackTrace()
                isRunning = false
                releaseWifiLock()
                
                // Notify on main thread that server failed
                CoroutineScope(Dispatchers.Main).launch {
                    onServerStarted?.invoke("Error: ${e.message ?: "Unknown error"}")
                }
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
            releaseWifiLock()
            Log.i(TAG, "Cast server stopped")
            onServerStopped?.invoke()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop server", e)
        }
    }
    
    private fun releaseWifiLock() {
        wifiLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "WiFi lock released")
            }
        }
        wifiLock = null
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
        Log.d(TAG, "broadcastEnemy called with: ${enemy.javaClass.simpleName}")
        Log.d(TAG, "Server running: $isRunning, Server null: ${server == null}")
        Log.d(TAG, "Connected clients: ${server?.getClientCount() ?: 0}")
        server?.broadcastEnemy(enemy)
    }
    
    /**
     * Broadcast persona data to all connected TVs
     */
    fun broadcastPersona(persona: Any) {
        Log.d(TAG, "broadcastPersona called with: ${persona.javaClass.simpleName}")
        Log.d(TAG, "Server running: $isRunning, Server null: ${server == null}")
        server?.broadcastPersona(persona)
    }
}
