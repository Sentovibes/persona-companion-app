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
                // Stop any previous server instance if hanging
                try {
                    server?.stop()
                } catch (_: Exception) {}

                val candidatePorts = listOf(8080, 8081, 8082, 8888, 8088, 9090)
                var activeServer: CastServer? = null
                var lastBindException: Exception? = null

                for (p in candidatePorts) {
                    try {
                        Log.d(TAG, "Attempting to start CastServer on port $p...")
                        val candidateServer = CastServer(context, p)
                        candidateServer.onClientConnected = this@CastManager.onClientConnected
                        candidateServer.onClientDisconnected = this@CastManager.onClientDisconnected
                        candidateServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                        activeServer = candidateServer
                        Log.i(TAG, "CastServer successfully bound to port $p")
                        break
                    } catch (e: java.net.BindException) {
                        Log.w(TAG, "Port $p is in use, trying next candidate port...", e)
                        lastBindException = e
                    } catch (e: java.io.IOException) {
                        if (e.message?.contains("Address already in use", ignoreCase = true) == true) {
                            Log.w(TAG, "Port $p is in use (Address already in use), trying next...", e)
                            lastBindException = e
                        } else {
                            throw e
                        }
                    }
                }

                if (activeServer == null) {
                    throw (lastBindException ?: Exception("All candidate ports are in use"))
                }

                server = activeServer
                isRunning = true
                
                val url = activeServer.getConnectionUrl()
                Log.i(TAG, "Cast server started successfully at: $url")
                
                // Notify on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    onServerStarted?.invoke(url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server", e)
                isRunning = false
                releaseWifiLock()
                
                // Notify on main thread that server failed
                CoroutineScope(Dispatchers.Main).launch {
                    onServerStarted?.invoke("Error: ${e.message ?: "Failed to bind cast port"}")
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
    fun broadcastEnemy(enemy: Any, gameId: String = "") {
        Log.d(TAG, "broadcastEnemy called with: ${enemy.javaClass.simpleName}")
        Log.d(TAG, "Server running: $isRunning, Server null: ${server == null}")
        Log.d(TAG, "Connected clients: ${server?.getClientCount() ?: 0}")
        server?.broadcastEnemy(enemy, gameId)
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
