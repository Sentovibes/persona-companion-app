package com.persona.companion.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.InputStream

object ImageUtils {
    private const val TAG = "ImageUtils"
    
    /**
     * Load image from assets
     * Returns null if image doesn't exist
     */
    fun loadImageFromAssets(context: Context, imagePath: String): Bitmap? {
        return try {
            val inputStream: InputStream = context.assets.open(imagePath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            Log.d(TAG, "Image not found: $imagePath")
            null
        }
    }
    
    /**
     * Get image path for persona/enemy
     * @param name The persona or enemy name
     * @param isEnemy true for enemies, false for personas
     */
    fun getImagePath(name: String, isEnemy: Boolean): String {
        val safeName = name.replace("/", "_").replace(":", "").replace("?", "")
        val folder = if (isEnemy) "enemies" else "personas"
        return "images/$folder/$safeName.png"
    }
    
    /**
     * Check if image exists in assets
     */
    fun imageExists(context: Context, imagePath: String): Boolean {
        return try {
            context.assets.open(imagePath).close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
