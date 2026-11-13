package com.persona.companion.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.persona.companion.BuildConfig
import java.io.File

object ImageUtils {
    private const val TAG = "ImageUtils"
    
    /**
     * Load bundled artwork from assets, falling back to legacy local files if present.
     * Returns null if image doesn't exist.
     */
    fun loadImageFromAssets(context: Context, imagePath: String): Bitmap? {
        return try {
            val altExtPath = if (imagePath.endsWith(".webp")) {
                imagePath.removeSuffix(".webp") + ".png"
            } else {
                imagePath.removeSuffix(".png") + ".webp"
            }

            if (BuildConfig.INCLUDE_IMAGES) {
                try {
                    context.assets.open(imagePath).use { BitmapFactory.decodeStream(it) }?.let { return it }
                } catch (_: Exception) {
                    try {
                        context.assets.open(altExtPath).use { BitmapFactory.decodeStream(it) }?.let { return it }
                    } catch (_: Exception) {}
                }
            }

            val legacyFile = File(context.filesDir, imagePath)
            if (legacyFile.exists()) {
                return BitmapFactory.decodeFile(legacyFile.absolutePath)
            }
            val altLegacy = File(context.filesDir, altExtPath)
            if (altLegacy.exists()) {
                return BitmapFactory.decodeFile(altLegacy.absolutePath)
            }
            null
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
            val altExtPath = if (imagePath.endsWith(".webp")) {
                imagePath.removeSuffix(".webp") + ".png"
            } else {
                imagePath.removeSuffix(".png") + ".webp"
            }

            if (BuildConfig.INCLUDE_IMAGES) {
                try {
                    context.assets.open(imagePath).close()
                    return true
                } catch (_: Exception) {
                    try {
                        context.assets.open(altExtPath).close()
                        return true
                    } catch (_: Exception) {}
                }
            }

            File(context.filesDir, imagePath).exists() || File(context.filesDir, altExtPath).exists()
        } catch (e: Exception) {
            false
        }
    }
}
