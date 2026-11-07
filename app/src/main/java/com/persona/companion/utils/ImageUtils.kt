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
            // Check downloaded files first (webp then png)
            val downloadedFile = File(context.filesDir, imagePath)
            if (downloadedFile.exists()) {
                return BitmapFactory.decodeFile(downloadedFile.absolutePath)
            }
            val altExtPath = if (imagePath.endsWith(".webp")) {
                imagePath.removeSuffix(".webp") + ".png"
            } else {
                imagePath.removeSuffix(".png") + ".webp"
            }
            val altDownloaded = File(context.filesDir, altExtPath)
            if (altDownloaded.exists()) {
                return BitmapFactory.decodeFile(altDownloaded.absolutePath)
            }

            // Fallback: try assets (only works if images were bundled)
            if (BuildConfig.INCLUDE_IMAGES) {
                try {
                    val inputStream: InputStream = context.assets.open(imagePath)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    bitmap
                } catch (_: Exception) {
                    val inputStream: InputStream = context.assets.open(altExtPath)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    bitmap
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "Image not found: $imagePath")
            null
        }
    }
    
    /**
     * Get primary image path for persona/enemy (.webp by default for 98.5% smaller footprint)
     */
    fun getImagePath(name: String, isEnemy: Boolean, gameId: String? = null): String {
        return getImagePathWithExt(name, isEnemy, gameId, "webp")
    }

    /**
     * Get fallback image path for legacy PNG assets
     */
    fun getImagePathPng(name: String, isEnemy: Boolean, gameId: String? = null): String {
        return getImagePathWithExt(name, isEnemy, gameId, "png")
    }

    private fun getImagePathWithExt(name: String, isEnemy: Boolean, gameId: String?, extension: String): String {
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
        // Note: hyphens are preserved as-is since persona filenames use them (e.g. ame-no-uzume.png)
        val safeName = cleanName.lowercase()
            .replace("è", "e").replace("é", "e").replace("ā", "a")
            .replace("ō", "o").replace("ū", "u").replace("î", "i")
            .replace(" ", "_")
            .replace("/", "_")
            .replace(":", "")
            .replace("?", "")
            .replace("'", "")
            .replace("\u2019", "")  // right single quotation mark
            .replace("&", "")
        
        val folder = if (isEnemy) "enemies_shared" else "personas_shared"
        
        // All images are now in shared folders, looked up by name only
        return "images/$folder/$safeName.$extension"
    }
    
    /**
     * Check if image exists in assets or downloaded files
     */
    fun imageExists(context: Context, imagePath: String): Boolean {
        return try {
            val file = File(context.filesDir, imagePath)
            if (file.exists()) return true

            val altExtPath = if (imagePath.endsWith(".webp")) {
                imagePath.removeSuffix(".webp") + ".png"
            } else {
                imagePath.removeSuffix(".png") + ".webp"
            }
            val altFile = File(context.filesDir, altExtPath)
            if (altFile.exists()) return true

            if (BuildConfig.INCLUDE_IMAGES) {
                try {
                    context.assets.open(imagePath).close()
                    true
                } catch (_: Exception) {
                    context.assets.open(altExtPath).close()
                    true
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
