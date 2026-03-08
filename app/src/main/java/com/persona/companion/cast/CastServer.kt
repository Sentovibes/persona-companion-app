package com.persona.companion.cast

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Custom Cast Server
 * Runs an HTTP server with WebSocket support for casting to TVs/browsers
 */
class CastServer(private val context: Context, port: Int = 8080) : NanoWSD(port) {
    
    private val TAG = "CastServer"
    private val gson = Gson()
    private val connectedClients = mutableListOf<CastWebSocket>()
    
    var onClientConnected: (() -> Unit)? = null
    var onClientDisconnected: (() -> Unit)? = null
    
    init {
        Log.d(TAG, "Cast server initialized on port $port")
    }
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            uri == "/" || uri == "/index.html" -> serveReceiverPage()
            uri == "/api/status" -> serveStatus()
            uri.startsWith("/assets/") -> serveAsset(uri)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }
    
    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        return CastWebSocket(handshake).also { socket ->
            connectedClients.add(socket)
            Log.d(TAG, "Client connected. Total clients: ${connectedClients.size}")
            onClientConnected?.invoke()
        }
    }
    
    /**
     * Serve the TV receiver HTML page
     */
    private fun serveReceiverPage(): Response {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Persona Companion - TV</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            color: #ffffff;
            overflow: hidden;
        }
        
        #container {
            width: 100vw;
            height: 100vh;
            display: flex;
            padding: 48px;
        }
        
        #stats-panel {
            flex: 1;
            padding-right: 48px;
            overflow-y: auto;
        }
        
        #image-panel {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 24px;
            padding: 48px;
        }
        
        h1 {
            font-size: 3.5em;
            margin-bottom: 24px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.5);
        }
        
        .stat-row {
            font-size: 1.8em;
            margin: 16px 0;
            padding: 16px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 12px;
            display: flex;
            justify-content: space-between;
        }
        
        .stat-label {
            color: #aaa;
        }
        
        .stat-value {
            font-weight: bold;
        }
        
        #connection-status {
            position: fixed;
            top: 24px;
            right: 24px;
            padding: 16px 32px;
            background: rgba(0, 255, 0, 0.2);
            border: 2px solid #00ff00;
            border-radius: 12px;
            font-size: 1.5em;
        }
        
        #connection-status.disconnected {
            background: rgba(255, 0, 0, 0.2);
            border-color: #ff0000;
        }
        
        .placeholder {
            font-size: 2em;
            color: #666;
            text-align: center;
        }
        
        #image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2.5em;
            color: #444;
        }
        
        .skill-list {
            margin-top: 24px;
        }
        
        .skill-item {
            font-size: 1.5em;
            padding: 12px;
            margin: 8px 0;
            background: rgba(100, 100, 255, 0.2);
            border-radius: 8px;
        }
    </style>
</head>
<body>
    <div id="connection-status" class="disconnected">Connecting...</div>
    
    <div id="container">
        <div id="stats-panel">
            <h1 id="enemy-name">Waiting for data...</h1>
            <div id="stats-content"></div>
        </div>
        
        <div id="image-panel">
            <div id="image-placeholder">Image Placeholder</div>
        </div>
    </div>
    
    <script>
        let ws = null;
        let reconnectInterval = null;
        
        function connect() {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = protocol + '//' + window.location.host + '/ws';
            
            ws = new WebSocket(wsUrl);
            
            ws.onopen = () => {
                console.log('Connected to cast server');
                document.getElementById('connection-status').textContent = 'Connected';
                document.getElementById('connection-status').classList.remove('disconnected');
                
                if (reconnectInterval) {
                    clearInterval(reconnectInterval);
                    reconnectInterval = null;
                }
            };
            
            ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    handleMessage(data);
                } catch (e) {
                    console.error('Failed to parse message:', e);
                }
            };
            
            ws.onclose = () => {
                console.log('Disconnected from cast server');
                document.getElementById('connection-status').textContent = 'Disconnected';
                document.getElementById('connection-status').classList.add('disconnected');
                
                // Auto-reconnect
                if (!reconnectInterval) {
                    reconnectInterval = setInterval(() => {
                        console.log('Attempting to reconnect...');
                        connect();
                    }, 3000);
                }
            };
            
            ws.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
        }
        
        function handleMessage(data) {
            if (data.type === 'enemy') {
                displayEnemy(data.enemy);
            } else if (data.type === 'persona') {
                displayPersona(data.persona);
            }
        }
        
        function displayEnemy(enemy) {
            document.getElementById('enemy-name').textContent = enemy.name;
            
            let statsHtml = '';
            statsHtml += '<div class="stat-row"><span class="stat-label">Level</span><span class="stat-value">' + enemy.level + '</span></div>';
            statsHtml += '<div class="stat-row"><span class="stat-label">HP</span><span class="stat-value">' + enemy.hp + '</span></div>';
            statsHtml += '<div class="stat-row"><span class="stat-label">SP</span><span class="stat-value">' + enemy.sp + '</span></div>';
            statsHtml += '<div class="stat-row"><span class="stat-label">Arcana</span><span class="stat-value">' + enemy.arcana + '</span></div>';
            
            if (enemy.skills && enemy.skills.length > 0) {
                statsHtml += '<div class="skill-list"><h2 style="font-size: 2em; margin: 24px 0 16px 0;">Skills</h2>';
                enemy.skills.forEach(skill => {
                    statsHtml += '<div class="skill-item">• ' + skill + '</div>';
                });
                statsHtml += '</div>';
            }
            
            document.getElementById('stats-content').innerHTML = statsHtml;
        }
        
        function displayPersona(persona) {
            document.getElementById('enemy-name').textContent = persona.name;
            
            let statsHtml = '';
            statsHtml += '<div class="stat-row"><span class="stat-label">Level</span><span class="stat-value">' + persona.level + '</span></div>';
            statsHtml += '<div class="stat-row"><span class="stat-label">Arcana</span><span class="stat-value">' + persona.arcana + '</span></div>';
            
            if (persona.skills && persona.skills.length > 0) {
                statsHtml += '<div class="skill-list"><h2 style="font-size: 2em; margin: 24px 0 16px 0;">Skills</h2>';
                persona.skills.forEach(skill => {
                    statsHtml += '<div class="skill-item">• ' + skill + '</div>';
                });
                statsHtml += '</div>';
            }
            
            document.getElementById('stats-content').innerHTML = statsHtml;
        }
        
        // Handle TV remote navigation
        document.addEventListener('keydown', (e) => {
            if (ws && ws.readyState === WebSocket.OPEN) {
                const message = {
                    type: 'remote_control',
                    key: e.key,
                    keyCode: e.keyCode
                };
                ws.send(JSON.stringify(message));
            }
        });
        
        // Connect on load
        connect();
    </script>
</body>
</html>
        """.trimIndent()
        
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }
    
    /**
     * Serve status endpoint
     */
    private fun serveStatus(): Response {
        val status = mapOf(
            "status" to "online",
            "clients" to connectedClients.size,
            "version" to "3.2.0"
        )
        return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(status))
    }
    
    /**
     * Serve static assets (future use)
     */
    private fun serveAsset(uri: String): Response {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Asset not found")
    }
    
    /**
     * Broadcast data to all connected clients
     */
    fun broadcastEnemy(enemy: Any) {
        val message = mapOf(
            "type" to "enemy",
            "enemy" to enemy
        )
        broadcast(gson.toJson(message))
    }
    
    fun broadcastPersona(persona: Any) {
        val message = mapOf(
            "type" to "persona",
            "persona" to persona
        )
        broadcast(gson.toJson(message))
    }
    
    private fun broadcast(message: String) {
        connectedClients.forEach { client ->
            try {
                client.send(message)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send message to client", e)
            }
        }
    }
    
    /**
     * Get local IP address
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val ip = address.hostAddress
                        if (ip?.contains(":") == false) { // IPv4 only
                            return ip
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get IP address", e)
        }
        return null
    }
    
    /**
     * Get connection URL for TV
     */
    fun getConnectionUrl(): String {
        val ip = getLocalIpAddress() ?: "localhost"
        return "http://$ip:$listeningPort"
    }
    
    /**
     * WebSocket handler
     */
    inner class CastWebSocket(handshake: IHTTPSession) : NanoWSD.WebSocket(handshake) {
        
        override fun onOpen() {
            Log.d(TAG, "WebSocket opened")
        }
        
        override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
            Log.d(TAG, "WebSocket closed: $reason")
            connectedClients.remove(this)
            onClientDisconnected?.invoke()
        }
        
        override fun onMessage(message: NanoWSD.WebSocketFrame) {
            val text = message.textPayload
            Log.d(TAG, "Received message: $text")
            // Handle remote control messages from TV
        }
        
        override fun onPong(pong: NanoWSD.WebSocketFrame) {
            // Handle pong
        }
        
        override fun onException(exception: IOException) {
            Log.e(TAG, "WebSocket exception", exception)
        }
    }
}
