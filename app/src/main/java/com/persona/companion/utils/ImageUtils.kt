package com.persona.companion.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.persona.companion.BuildConfig
import java.io.File
import java.io.InputStream

object ImageUtils {
    private const val TAG = "ImageUtils"
    
    /**
     * Load image from assets (debug) or downloaded files (release)
     * Returns null if image doesn't exist
     */
    fun loadImageFromAssets(context: Context, imagePath: String): Bitmap? {
        return try {
            if (BuildConfig.INCLUDE_IMAGES) {
                // Images bundled in APK: Load from assets
                val inputStream: InputStream = context.assets.open(imagePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                bitmap
            } else {
                // Images not bundled: Load from downloaded files
                val downloadedFile = File(context.filesDir, imagePath)
                if (downloadedFile.exists()) {
                    BitmapFactory.decodeFile(downloadedFile.absolutePath)
                } else {
                    // No fallback - images must be downloaded
                    null
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Image not found: $imagePath")
            null
        }
    }
    
    /**
     * Get image path for persona/enemy
     * @param name The persona or enemy name
     * @param isEnemy true for enemies, false for personas
     * @param gameId Optional game ID for game-specific image handling
     */
    fun getImagePath(name: String, isEnemy: Boolean, gameId: String? = null): String {
        // Remove variant suffixes (A, B, C, etc.) for enemies
        var cleanName = name
        if (isEnemy && name.matches(Regex(".*\\s[A-Z]$"))) {
            cleanName = name.substring(0, name.length - 2).trim()
        }
        
        // Special handling for compound boss names (use first part only)
        if (isEnemy && cleanName.contains(" & ")) {
            cleanName = cleanName.split(" & ")[0]
        }
        
        // Special handling for boss names with full names (ONLY for P3R)
        if (isEnemy && gameId == "p3r") {
            val bossNameMap = mapOf(
                "chidori yoshino" to "chidori",
                "jin shirato" to "jin",
                "takaya sakaki" to "takaya"
            )
            
            val lowerName = cleanName.lowercase()
            if (bossNameMap.containsKey(lowerName)) {
                cleanName = bossNameMap[lowerName]!!
            }
        }
        
        // Convert name to lowercase and replace special characters
        val safeName = cleanName.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace("/", "_")
            .replace(":", "")
            .replace("?", "")
            .replace("'", "")
            .replace("'", "")
            .replace("&", "")
        
        val folder = if (isEnemy) "enemies_shared" else "personas_shared"
        
        // All images are now in shared folders, looked up by name only
        return "images/$folder/$safeName.png"
    }
    
    /**
     * Check if image exists in assets or downloaded files
     */
    fun imageExists(context: Context, imagePath: String): Boolean {
        return try {
            if (BuildConfig.INCLUDE_IMAGES) {
                // Images bundled: check assets
                context.assets.open(imagePath).close()
                true
            } else {
                // Images not bundled: check downloaded files
                val downloadedFile = File(context.filesDir, imagePath)
                downloadedFile.exists()
            }
        } catch (e: Exception) {
            false
        }
    }
}
