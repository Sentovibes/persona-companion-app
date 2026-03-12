package com.persona.companion.utils

import android.content.Context
import android.util.Log
import coil.request.ImageRequest
import com.persona.companion.BuildConfig
import com.persona.companion.R
import java.io.File

/**
 * Extension functions for Coil ImageRequest to load persona and enemy images.
 * 
 * These extensions handle:
 * - Loading images from assets when bundled (debug builds)
 * - Loading images from internal storage when downloaded (release builds)
 * - Falling back to placeholder images when not available
 */

/**
 * Configure this ImageRequest to load a persona image.
 * 
 * @param context Android context
 * @param game Game identifier (not used - images are in shared folder)
 * @param name Persona name
 * @return This ImageRequest.Builder for chaining
 */
fun ImageRequest.Builder.personaImage(
    context: Context,
    game: String,
    name: String
): ImageRequest.Builder {
    val imagePath = ImageUtils.getImagePath(name, isEnemy = false, gameId = game)
    val downloadedFile = File(context.filesDir, imagePath)
    
    // Simple: if file exists, load it. Otherwise, use placeholder.
    return if (downloadedFile.exists()) {
        this.data(downloadedFile)
    } else {
        this.data(R.drawable.placeholder_persona)
    }
}

/**
 * Configure this ImageRequest to load an enemy image.
 * 
 * @param context Android context
 * @param game Game identifier
 * @param name Enemy name
 * @return This ImageRequest.Builder for chaining
 */
fun ImageRequest.Builder.enemyImage(
    context: Context,
    game: String,
    name: String
): ImageRequest.Builder {
    val imagePath = ImageUtils.getImagePath(name, isEnemy = true, gameId = game)
    val downloadedFile = File(context.filesDir, imagePath)
    
    // Simple: if file exists, load it. Otherwise, use placeholder.
    return if (downloadedFile.exists()) {
        this.data(downloadedFile)
    } else {
        this.data(R.drawable.placeholder_enemy)
    }
}

/**
 * Get the file path for a persona image.
 * 
 * @param context Android context
 * @param game Game identifier
 * @param name Persona name
 * @return File object for the image (may not exist)
 */
fun getPersonaImagePath(context: Context, game: String, name: String): File {
    val imagePath = ImageUtils.getImagePath(name, isEnemy = false, gameId = game)
    return File(context.filesDir, imagePath)
}

/**
 * Get the file path for an enemy image.
 * 
 * @param context Android context
 * @param game Game identifier
 * @param name Enemy name
 * @return File object for the image (may not exist)
 */
fun getEnemyImagePath(context: Context, game: String, name: String): File {
    val imagePath = ImageUtils.getImagePath(name, isEnemy = true, gameId = game)
    return File(context.filesDir, imagePath)
}
