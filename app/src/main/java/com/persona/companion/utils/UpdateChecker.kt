package com.persona.companion.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/Sentovibes/persona-companion-app/releases/latest"
    private const val CURRENT_VERSION = "3.1.5"
    
    suspend fun checkForUpdates(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(GITHUB_API_URL).openConnection()
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            
            val latestVersion = json.getString("tag_name").removePrefix("v")
            val releaseNotes = json.optString("body", "No release notes available")
            val assets = json.getJSONArray("assets")
            
            var downloadUrl = ""
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith(".apk")) {
                    downloadUrl = asset.getString("browser_download_url")
                    break
                }
            }
            
            val isUpdateAvailable = compareVersions(latestVersion, CURRENT_VERSION) > 0
            
            Result.success(
                UpdateInfo(
                    latestVersion = latestVersion,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                    isUpdateAvailable = isUpdateAvailable
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrNull(i) ?: 0
            val p2 = parts2.getOrNull(i) ?: 0
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
