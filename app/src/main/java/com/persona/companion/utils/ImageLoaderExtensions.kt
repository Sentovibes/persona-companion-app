package com.persona.companion.utils

import android.content.Context
import coil.request.ImageRequest
import com.persona.companion.BuildConfig
import com.persona.companion.R
import java.io.File

/**
 * Configure this ImageRequest to load a persona image.
 * Prefers bundled WebP artwork, with fallback to legacy local files if present.
 */
fun ImageRequest.Builder.personaImage(
    context: Context,
    game: String,
    name: String
): ImageRequest.Builder {
    val webpPath = ImageUtils.getImagePath(name, isEnemy = false, gameId = game)
    val pngPath = ImageUtils.getImagePathPng(name, isEnemy = false, gameId = game)

    val legacyWebp = File(context.filesDir, webpPath)
    val legacyPng = File(context.filesDir, pngPath)

    return when {
        BuildConfig.INCLUDE_IMAGES -> this.data("file:///android_asset/$webpPath")
        legacyWebp.exists() -> this.data(legacyWebp)
        legacyPng.exists() -> this.data(legacyPng)
        else -> this.data(R.drawable.placeholder_persona)
    }
}

/**
 * Configure this ImageRequest to load an enemy image.
 * Prefers bundled WebP artwork, with fallback to legacy local files if present.
 */
fun ImageRequest.Builder.enemyImage(
    context: Context,
    game: String,
    name: String
): ImageRequest.Builder {
    val webpPath = ImageUtils.getImagePath(name, isEnemy = true, gameId = game)
    val pngPath = ImageUtils.getImagePathPng(name, isEnemy = true, gameId = game)

    val legacyWebp = File(context.filesDir, webpPath)
    val legacyPng = File(context.filesDir, pngPath)

    return when {
        BuildConfig.INCLUDE_IMAGES -> this.data("file:///android_asset/$webpPath")
        legacyWebp.exists() -> this.data(legacyWebp)
        legacyPng.exists() -> this.data(legacyPng)
        else -> this.data(R.drawable.placeholder_enemy)
    }
}

/**
 * Get the file path for a persona image.
 */
fun getPersonaImagePath(context: Context, game: String, name: String): File {
    val webpPath = ImageUtils.getImagePath(name, isEnemy = false, gameId = game)
    val webpFile = File(context.filesDir, webpPath)
    if (webpFile.exists()) return webpFile
    val pngPath = ImageUtils.getImagePathPng(name, isEnemy = false, gameId = game)
    return File(context.filesDir, pngPath)
}

/**
 * Get the file path for an enemy image.
 */
fun getEnemyImagePath(context: Context, game: String, name: String): File {
    val webpPath = ImageUtils.getImagePath(name, isEnemy = true, gameId = game)
    val webpFile = File(context.filesDir, webpPath)
    if (webpFile.exists()) return webpFile
    val pngPath = ImageUtils.getImagePathPng(name, isEnemy = true, gameId = game)
    return File(context.filesDir, pngPath)
}
