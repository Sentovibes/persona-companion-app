package com.persona.companion.cast

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.persona.companion.utils.ImageUtils
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        // Set socket timeout to infinite (0 = no timeout)
        try {
            setAsyncRunner(NanoHTTPD.DefaultAsyncRunner())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set async runner", e)
        }
    }
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.d(TAG, "Serving request: $uri")
        Log.d(TAG, "Headers: ${session.headers}")
        
        // Check if this is a WebSocket upgrade request
        val upgradeHeader = session.headers["upgrade"]
        if (upgradeHeader != null && upgradeHeader.equals("websocket", ignoreCase = true)) {
            Log.d(TAG, "WebSocket upgrade request detected")
            return super.serve(session) // Let NanoWSD handle WebSocket upgrade
        }
        
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
            background: #0F0F0F;
            color: #EEEEEE;
            overflow: hidden;
            height: 100vh;
        }
        
        #connection-status {
            position: fixed;
            top: 1vh;
            right: 1vw;
            padding: 0.5vh 1vw;
            background: rgba(0, 200, 0, 0.2);
            border: 2px solid #00c800;
            border-radius: 0.5vh;
            font-size: clamp(0.7rem, 1.5vh, 1rem);
            font-weight: 600;
            z-index: 1000;
        }
        
        #connection-status.disconnected {
            background: rgba(200, 0, 0, 0.2);
            border-color: #c80000;
        }
        
        #test-button {
            position: fixed;
            top: 1vh;
            left: 1vw;
            padding: 0.5vh 1vw;
            background: rgba(26, 111, 204, 0.3);
            border: 2px solid #1A6FCC;
            border-radius: 0.5vh;
            font-size: clamp(0.7rem, 1.5vh, 1rem);
            font-weight: 600;
            z-index: 1000;
            cursor: pointer;
            color: #EEEEEE;
        }
        
        #test-button:hover {
            background: rgba(26, 111, 204, 0.5);
        }
        
        #container {
            width: 100vw;
            height: 100vh;
            display: grid;
            grid-template-columns: 1fr 1fr 0.6fr;
            grid-template-rows: auto 1fr 1fr;
            padding: 6vh 3vw 3vh 3vw;
            gap: 1.5vw;
        }
        
        h1 {
            grid-column: 1 / -1;
            font-size: clamp(1.5rem, 4vh, 3rem);
            margin-bottom: 0;
            color: #EEEEEE;
            font-weight: 600;
        }
        
        .info-section {
            background: #1A1A1A;
            border-radius: 1vh;
            padding: 1.5vh 1.5vw;
            border: 1px solid #333333;
            overflow-y: auto;
        }
        
        .info-section h2 {
            font-size: clamp(0.9rem, 1.8vh, 1.2rem);
            margin-bottom: 1vh;
            color: #EEEEEE;
            font-weight: 600;
            border-bottom: 2px solid #333333;
            padding-bottom: 0.5vh;
        }
        
        #basic-info {
            grid-column: 1;
            grid-row: 2;
        }
        
        #stats-info {
            grid-column: 2;
            grid-row: 2;
        }
        
        #resistances-info {
            grid-column: 1;
            grid-row: 3;
        }
        
        #skills-info {
            grid-column: 2;
            grid-row: 3;
        }
        
        #image-panel {
            grid-column: 3;
            grid-row: 2 / 4;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #1A1A1A;
            border-radius: 1.5vh;
            padding: 3vh;
            border: 1px solid #333333;
        }
        
        .stat-row {
            font-size: clamp(0.7rem, 1.4vh, 0.95rem);
            margin: 0.4vh 0;
            padding: 0.8vh 1vw;
            background: #222222;
            border-radius: 0.6vh;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border: 1px solid #333333;
        }
        
        .stat-label {
            color: #9E9E9E;
            font-weight: 500;
        }
        
        .stat-value {
            font-weight: 600;
            color: #EEEEEE;
        }
        
        .resist-item {
            font-size: clamp(0.7rem, 1.4vh, 0.95rem);
            padding: 0.6vh 1vw;
            margin: 0.3vh 0;
            background: #222222;
            border-radius: 0.6vh;
            border-left: 0.3vw solid #1A6FCC;
            color: #EEEEEE;
        }
        
        .skill-item {
            font-size: clamp(0.7rem, 1.4vh, 0.95rem);
            padding: 0.6vh 1vw;
            margin: 0.3vh 0;
            background: #222222;
            border-radius: 0.6vh;
            border-left: 0.3vw solid #1A6FCC;
            color: #EEEEEE;
        }
        
        .placeholder {
            font-size: clamp(0.8rem, 1.6vh, 1rem);
            color: #555555;
            text-align: center;
            padding: 2vh;
        }
        
        #image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            font-size: clamp(1rem, 2.5vh, 1.5rem);
            color: #555555;
            text-align: center;
        }
        
        #image-placeholder .icon {
            font-size: clamp(2rem, 6vh, 4rem);
            margin-bottom: 2vh;
            opacity: 0.5;
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.6; }
        }
        
        .waiting {
            animation: pulse 2s ease-in-out infinite;
        }
    </style>
</head>
<body>
    <button id="test-button" onclick="testData()">Test Data</button>
    <div id="connection-status" class="disconnected">Connecting...</div>
    
    <div id="container">
        <h1 id="enemy-name" class="waiting">Waiting for data...</h1>
        
        <div id="basic-info" class="info-section">
            <h2>Basic Info</h2>
            <div id="basic-content">
                <div class="placeholder">Browse on phone to display here</div>
            </div>
        </div>
        
        <div id="stats-info" class="info-section">
            <h2>Stats</h2>
            <div id="stats-content">
                <div class="placeholder">Browse on phone to display here</div>
            </div>
        </div>
        
        <div id="resistances-info" class="info-section">
            <h2>Resistances</h2>
            <div id="resistances-content">
                <div class="placeholder">Browse on phone to display here</div>
            </div>
        </div>
        
        <div id="skills-info" class="info-section">
            <h2>Skills</h2>
            <div id="skills-content">
                <div class="placeholder">Browse on phone to display here</div>
            </div>
        </div>
        
        <div id="image-panel">
            <div id="image-placeholder">
                <div class="icon">📱 → 📺</div>
                <div>Browse on phone</div>
                <div style="font-size: 0.7em; margin-top: 1vh; opacity: 0.7;">to display here</div>
            </div>
        </div>
    </div>
    
    <script>
        let ws = null;
        let reconnectInterval = null;
        let pingInterval = null;
        
        function connect() {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = protocol + '//' + window.location.host;
            
            console.log('Connecting to:', wsUrl);
            ws = new WebSocket(wsUrl);
            
            ws.onopen = () => {
                console.log('Connected to cast server');
                document.getElementById('connection-status').textContent = '✓ Connected';
                document.getElementById('connection-status').classList.remove('disconnected');
                
                if (reconnectInterval) {
                    clearInterval(reconnectInterval);
                    reconnectInterval = null;
                }
                
                // Start ping interval to keep connection alive
                if (pingInterval) {
                    clearInterval(pingInterval);
                }
                // Start pinging immediately and then every 10 seconds
                setTimeout(() => {
                    if (ws && ws.readyState === WebSocket.OPEN) {
                        try {
                            console.log('Sending initial ping...');
                            ws.send(JSON.stringify({ type: 'ping' }));
                        } catch (e) {
                            console.error('Failed to send initial ping:', e);
                        }
                    }
                }, 1000); // First ping after 1 second
                
                pingInterval = setInterval(() => {
                    if (ws && ws.readyState === WebSocket.OPEN) {
                        try {
                            console.log('Sending ping...');
                            ws.send(JSON.stringify({ type: 'ping' }));
                        } catch (e) {
                            console.error('Failed to send ping:', e);
                        }
                    } else {
                        console.warn('WebSocket not open, cannot send ping');
                        if (pingInterval) {
                            clearInterval(pingInterval);
                            pingInterval = null;
                        }
                    }
                }, 10000); // Ping every 10 seconds
            };
            
            ws.onmessage = (event) => {
                console.log('Raw message received:', event.data);
                try {
                    const data = JSON.parse(event.data);
                    console.log('Parsed data:', data);
                    console.log('Data type:', data.type);
                    
                    if (data.type === 'pong') {
                        console.log('Received pong');
                        return;
                    }
                    
                    if (data.type === 'welcome') {
                        console.log('Received welcome:', data.message);
                        // Send acknowledgment to keep connection alive
                        if (ws && ws.readyState === WebSocket.OPEN) {
                            ws.send(JSON.stringify({ type: 'ack', message: 'ready' }));
                            console.log('Sent acknowledgment');
                        }
                        return;
                    }
                    
                    console.log('Data keys:', Object.keys(data));
                    handleMessage(data);
                } catch (e) {
                    console.error('Failed to parse message:', e, event.data);
                }
            };
            
            ws.onclose = (event) => {
                console.log('Disconnected from cast server');
                console.log('Close code:', event.code, 'Reason:', event.reason);
                document.getElementById('connection-status').textContent = '✗ Disconnected';
                document.getElementById('connection-status').classList.add('disconnected');
                
                // Clear ping interval
                if (pingInterval) {
                    clearInterval(pingInterval);
                    pingInterval = null;
                }
                
                // Auto-reconnect
                if (!reconnectInterval) {
                    console.log('Starting reconnect interval...');
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
            console.log('Handling message type:', data.type);
            if (data.type === 'enemy') {
                displayEnemy(data.enemy);
            } else if (data.type === 'persona') {
                displayPersona(data.persona);
            }
        }
        
        function displayEnemy(enemy) {
            console.log('Displaying enemy:', enemy);
            
            const nameEl = document.getElementById('enemy-name');
            nameEl.textContent = enemy.name || 'Unknown Enemy';
            nameEl.classList.remove('waiting');
            
            // Hide the placeholder on the right
            const imagePlaceholder = document.getElementById('image-placeholder');
            if (imagePlaceholder) {
                imagePlaceholder.style.display = 'none';
            }
            
            // ── BASIC INFO (Top-left) ────────────────────────────────────
            let basicHtml = '';
            if (enemy.level !== undefined) {
                basicHtml += '<div class="stat-row"><span class="stat-label">Level</span><span class="stat-value">' + enemy.level + '</span></div>';
            }
            if (enemy.hp !== undefined) {
                basicHtml += '<div class="stat-row"><span class="stat-label">HP</span><span class="stat-value">' + enemy.hp + '</span></div>';
            }
            if (enemy.sp !== undefined) {
                basicHtml += '<div class="stat-row"><span class="stat-label">SP</span><span class="stat-value">' + enemy.sp + '</span></div>';
            }
            if (enemy.arcana) {
                basicHtml += '<div class="stat-row"><span class="stat-label">Arcana</span><span class="stat-value">' + enemy.arcana + '</span></div>';
            }
            if (enemy.area && enemy.area !== 'Unknown') {
                basicHtml += '<div class="stat-row"><span class="stat-label">Area</span><span class="stat-value">' + enemy.area + '</span></div>';
            }
            if (enemy.exp && enemy.exp > 0) {
                basicHtml += '<div class="stat-row"><span class="stat-label">EXP</span><span class="stat-value">' + enemy.exp + '</span></div>';
            }
            if (enemy.drops) {
                if (enemy.drops.gem && enemy.drops.gem !== '-') {
                    basicHtml += '<div class="stat-row"><span class="stat-label">Gem</span><span class="stat-value">' + enemy.drops.gem + '</span></div>';
                }
                if (enemy.drops.item && enemy.drops.item !== '-') {
                    basicHtml += '<div class="stat-row"><span class="stat-label">Item</span><span class="stat-value">' + enemy.drops.item + '</span></div>';
                }
            }
            document.getElementById('basic-content').innerHTML = basicHtml || '<div class="placeholder">No basic info</div>';
            
            // ── STATS (Top-right) ────────────────────────────────────────
            let statsHtml = '';
            if (enemy.stats) {
                if (enemy.stats.strength !== undefined) {
                    statsHtml += '<div class="stat-row"><span class="stat-label">Strength</span><span class="stat-value">' + enemy.stats.strength + '</span></div>';
                }
                if (enemy.stats.magic !== undefined) {
                    statsHtml += '<div class="stat-row"><span class="stat-label">Magic</span><span class="stat-value">' + enemy.stats.magic + '</span></div>';
                }
                if (enemy.stats.endurance !== undefined) {
                    statsHtml += '<div class="stat-row"><span class="stat-label">Endurance</span><span class="stat-value">' + enemy.stats.endurance + '</span></div>';
                }
                if (enemy.stats.agility !== undefined) {
                    statsHtml += '<div class="stat-row"><span class="stat-label">Agility</span><span class="stat-value">' + enemy.stats.agility + '</span></div>';
                }
                if (enemy.stats.luck !== undefined) {
                    statsHtml += '<div class="stat-row"><span class="stat-label">Luck</span><span class="stat-value">' + enemy.stats.luck + '</span></div>';
                }
            }
            document.getElementById('stats-content').innerHTML = statsHtml || '<div class="placeholder">No stats</div>';
            
            // ── RESISTANCES (Bottom-left) ────────────────────────────────
            let resistHtml = '';
            if (enemy.resists) {
                const parsed = parseResistances(enemy.resists);
                if (parsed) {
                    const resistances = parsed.split(', ');
                    resistances.forEach(resist => {
                        resistHtml += '<div class="resist-item">' + resist + '</div>';
                    });
                } else {
                    resistHtml += '<div class="resist-item">' + enemy.resists + '</div>';
                }
            }
            document.getElementById('resistances-content').innerHTML = resistHtml || '<div class="placeholder">No resistances</div>';
            
            // ── SKILLS (Bottom-right) ────────────────────────────────────
            let skillsHtml = '';
            if (enemy.skills && enemy.skills.length > 0) {
                enemy.skills.forEach(skill => {
                    skillsHtml += '<div class="skill-item">' + skill + '</div>';
                });
            }
            document.getElementById('skills-content').innerHTML = skillsHtml || '<div class="placeholder">No skills</div>';
            
            // ── IMAGE (Right panel) ───────────────────────────────────────
            const imagePanel = document.getElementById('image-panel');
            if (enemy.image) {
                imagePanel.innerHTML = '<img src="' + enemy.image + '" style="max-width: 100%; max-height: 100%; object-fit: contain;" alt="' + enemy.name + '">';
            } else {
                imagePanel.innerHTML = '<div id="image-placeholder"><div class="icon">🎭</div><div>' + enemy.name + '</div><div style="font-size: 0.7em; margin-top: 1vh; opacity: 0.7;">No image available</div></div>';
            }
        }
        
        function displayPersona(persona) {
            console.log('Displaying persona:', persona.name);
            const nameEl = document.getElementById('enemy-name');
            nameEl.textContent = persona.name;
            nameEl.classList.remove('waiting');
            
            let statsHtml = '';
            statsHtml += '<div class="stat-row"><span class="stat-label">Level</span><span class="stat-value">' + persona.level + '</span></div>';
            statsHtml += '<div class="stat-row"><span class="stat-label">Arcana</span><span class="stat-value">' + persona.arcana + '</span></div>';
            
            if (persona.skills && persona.skills.length > 0) {
                statsHtml += '<div class="skill-list"><h2>Skills</h2>';
                persona.skills.forEach(skill => {
                    statsHtml += '<div class="skill-item">• ' + skill + '</div>';
                });
                statsHtml += '</div>';
            }
            
            document.getElementById('stats-content').innerHTML = statsHtml;
        }
        
        // Handle TV remote navigation
        document.addEventListener('keydown', (e) => {
            console.log('Key pressed:', e.key);
            if (ws && ws.readyState === WebSocket.OPEN) {
                const message = {
                    type: 'remote_control',
                    key: e.key,
                    keyCode: e.keyCode
                };
                ws.send(JSON.stringify(message));
            }
        });
        
        // Test data function
        function testData() {
            console.log('Testing with sample data...');
            const testEnemy = {
                name: 'Shadow Yukiko',
                level: 15,
                hp: 1500,
                sp: 450,
                arcana: 'Priestess',
                area: 'Yukiko\'s Castle',
                exp: 500,
                stats: {
                    strength: 12,
                    magic: 18,
                    endurance: 10,
                    agility: 14,
                    luck: 11
                },
                resists: 'w--w__-ww-',
                skills: [
                    'Agi',
                    'Maragi',
                    'Burn to Ashes',
                    'White Wall',
                    'Red Wall',
                    'Fire Boost'
                ],
                drops: {
                    gem: 'Fire Gem',
                    item: 'Chakra Ring'
                }
            };
            displayEnemy(testEnemy);
        }
        
        // Parse resistance string to readable format
        function parseResistances(resists) {
            if (!resists || resists.length === 0) return null;
            
            // P3 elements: Slash, Strike, Pierce, Fire, Ice, Elec, Wind, Light, Dark, Almighty
            const p3Elements = ['Slash', 'Strike', 'Pierce', 'Fire', 'Ice', 'Elec', 'Wind', 'Light', 'Dark', 'Almighty'];
            // P4 elements: Phys, Fire, Ice, Elec, Wind, Light, Dark, Almighty
            const p4Elements = ['Phys', 'Fire', 'Ice', 'Elec', 'Wind', 'Light', 'Dark', 'Almighty'];
            // P5 elements: Phys, Gun, Fire, Ice, Elec, Wind, Psy, Nuke, Bless, Curse
            const p5Elements = ['Phys', 'Gun', 'Fire', 'Ice', 'Elec', 'Wind', 'Psy', 'Nuke', 'Bless', 'Curse'];
            
            // Determine which element set to use based on length
            let elements;
            if (resists.length === 10) {
                // Could be P3 or P5, check for common P5 patterns
                elements = p3Elements; // Default to P3 for 10-length
            } else if (resists.length === 8) {
                elements = p4Elements;
            } else {
                elements = p4Elements; // Default
            }
            
            const resistMap = {
                '-': 'Normal',
                '_': 'Normal',
                'w': 'Weak',
                's': 'Strong',
                'r': 'Resist',
                'n': 'Null',
                'd': 'Drain',
                'p': 'Repel'
            };
            
            const parsed = [];
            for (let i = 0; i < resists.length && i < elements.length; i++) {
                const char = resists[i].toLowerCase();
                const resist = resistMap[char];
                if (resist) {
                    parsed.push(elements[i] + ': ' + resist);
                }
            }
            
            return parsed.length > 0 ? parsed.join(', ') : 'All Normal';
        }
        
        // Connect on load
        console.log('Page loaded, connecting...');
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
            "version" to "4.0.0"
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create a simple map with only basic data
                // Avoid complex nested objects that cause serialization issues
                val enemyMap = mutableMapOf<String, Any?>()
                
                // Use reflection to get properties safely
                val enemyClass = enemy.javaClass
                try {
                    val enemyName = enemyClass.getMethod("getName").invoke(enemy) as? String ?: "Unknown"
                    enemyMap["name"] = enemyName
                    enemyMap["level"] = enemyClass.getMethod("getLevel").invoke(enemy) ?: 0
                    enemyMap["hp"] = enemyClass.getMethod("getHp").invoke(enemy) ?: 0
                    enemyMap["sp"] = enemyClass.getMethod("getSp").invoke(enemy) ?: 0
                    enemyMap["arcana"] = enemyClass.getMethod("getArcana").invoke(enemy) ?: ""
                    enemyMap["area"] = enemyClass.getMethod("getArea").invoke(enemy) ?: ""
                    enemyMap["exp"] = enemyClass.getMethod("getExp").invoke(enemy) ?: 0
                    enemyMap["resists"] = enemyClass.getMethod("getResists").invoke(enemy) ?: ""
                    
                    // Load and encode image as base64
                    try {
                        val imagePath = ImageUtils.getImagePath(enemyName, isEnemy = true)
                        val bitmap = ImageUtils.loadImageFromAssets(context, imagePath)
                        if (bitmap != null) {
                            val outputStream = java.io.ByteArrayOutputStream()
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, outputStream)
                            val imageBytes = outputStream.toByteArray()
                            val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                            enemyMap["image"] = "data:image/png;base64,$base64Image"
                            Log.d(TAG, "Image encoded: ${imageBytes.size} bytes")
                        } else {
                            Log.d(TAG, "No image found for: $enemyName")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load image", e)
                    }
                    
                    // Skills - convert to simple list
                    val skills = enemyClass.getMethod("getSkills").invoke(enemy)
                    if (skills is List<*>) {
                        enemyMap["skills"] = skills.filterIsInstance<String>()
                    } else {
                        enemyMap["skills"] = emptyList<String>()
                    }
                    
                    // Stats - convert to simple map
                    try {
                        val stats = enemyClass.getMethod("getStats").invoke(enemy)
                        if (stats != null) {
                            val statsClass = stats.javaClass
                            enemyMap["stats"] = mapOf(
                                "strength" to (statsClass.getMethod("getStrength").invoke(stats) ?: 0),
                                "magic" to (statsClass.getMethod("getMagic").invoke(stats) ?: 0),
                                "endurance" to (statsClass.getMethod("getEndurance").invoke(stats) ?: 0),
                                "agility" to (statsClass.getMethod("getAgility").invoke(stats) ?: 0),
                                "luck" to (statsClass.getMethod("getLuck").invoke(stats) ?: 0)
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "No stats available")
                    }
                    
                    // Drops - convert to simple map
                    try {
                        val drops = enemyClass.getMethod("getDrops").invoke(enemy)
                        if (drops != null) {
                            val dropsClass = drops.javaClass
                            enemyMap["drops"] = mapOf(
                                "gem" to (dropsClass.getMethod("getGem").invoke(drops) ?: "-"),
                                "item" to (dropsClass.getMethod("getItem").invoke(drops) ?: "-")
                            )
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "No drops available")
                    }
                    
                    // Skip phases and parts for now - they're too complex and cause freezing
                    // TODO: Simplify phases/parts serialization later
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to extract enemy properties", e)
                }
                
                val message = mapOf(
                    "type" to "enemy",
                    "enemy" to enemyMap
                )
                val json = gson.toJson(message)
                Log.d(TAG, "Broadcasting enemy: ${enemyMap["name"]}")
                Log.d(TAG, "JSON length: ${json.length}")
                Log.d(TAG, "Connected clients: ${connectedClients.size}")
                broadcast(json)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast enemy", e)
            }
        }
    }
    
    fun broadcastPersona(persona: Any) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val message = mapOf(
                    "type" to "persona",
                    "persona" to persona
                )
                val json = gson.toJson(message)
                Log.d(TAG, "Broadcasting persona JSON (length: ${json.length})")
                broadcast(json)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast persona", e)
            }
        }
    }
    
    private fun broadcast(message: String) {
        Log.d(TAG, "Broadcasting to ${connectedClients.size} clients")
        connectedClients.forEach { client ->
            try {
                Log.d(TAG, "Sending to client...")
                client.send(message)
                Log.d(TAG, "Sent successfully")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send message to client", e)
            }
        }
    }
    
    /**
     * Get number of connected clients
     */
    fun getClientCount(): Int = connectedClients.size
    
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
    inner class CastWebSocket(private val handshakeSession: IHTTPSession) : NanoWSD.WebSocket(handshakeSession) {
        
        override fun onOpen() {
            Log.d(TAG, "WebSocket opened!")
            Log.d(TAG, "Client IP: ${handshakeSession.remoteIpAddress}")
            Log.d(TAG, "Total clients now: ${connectedClients.size}")
            
            // Send welcome message to keep connection alive
            try {
                send("""{"type":"welcome","message":"Connected to Persona Companion Cast"}""")
                Log.d(TAG, "Sent welcome message")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send welcome message", e)
            }
            
            // Start server-side keepalive - send ping every 3 seconds
            CoroutineScope(Dispatchers.IO).launch {
                while (isOpen) {
                    try {
                        kotlinx.coroutines.delay(3000) // Ping every 3 seconds
                        if (isOpen) {
                            ping("keepalive".toByteArray())
                            Log.d(TAG, "Sent server ping")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Keepalive failed", e)
                        break
                    }
                }
            }
        }
        
        override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
            Log.d(TAG, "WebSocket closed: $reason (initiated by remote: $initiatedByRemote)")
            connectedClients.remove(this)
            Log.d(TAG, "Total clients now: ${connectedClients.size}")
            onClientDisconnected?.invoke()
        }
        
        override fun onMessage(message: NanoWSD.WebSocketFrame) {
            val text = message.textPayload
            Log.d(TAG, "Received message from TV: $text")
            
            // Handle ping/pong for keepalive
            try {
                val json = com.google.gson.JsonParser.parseString(text).asJsonObject
                if (json.has("type") && json.get("type").asString == "ping") {
                    Log.d(TAG, "Received ping, sending pong")
                    send("""{"type":"pong"}""")
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "Not a JSON message or no type field")
            }
            
            // Handle other messages (remote control, etc.)
        }
        
        override fun onPong(pong: NanoWSD.WebSocketFrame) {
            // Handle pong
        }
        
        override fun onException(exception: IOException) {
            Log.e(TAG, "WebSocket exception", exception)
        }
    }
}
