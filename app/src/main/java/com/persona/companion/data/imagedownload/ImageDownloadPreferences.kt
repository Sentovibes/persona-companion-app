package com.persona.companion.data.imagedownload

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing image download state persistence using SharedPreferences.
 * 
 * This class provides a type-safe interface for storing and retrieving download state,
 * including completion status, version information, and partial download progress.
 */
class ImageDownloadPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "image_downloads"
        
        // Keys for download completion state
        private const val KEY_IMAGES_DOWNLOADED = "images_downloaded"
        private const val KEY_DOWNLOADED_VERSION = "downloaded_version"
        private const val KEY_DOWNLOAD_TIMESTAMP = "download_timestamp"
        
        // Keys for partial download state (for resume functionality)
        private const val KEY_PARTIAL_DOWNLOAD_BYTES = "partial_download_bytes"
        private const val KEY_PARTIAL_DOWNLOAD_TOTAL_BYTES = "partial_download_total_bytes"
        private const val KEY_PARTIAL_DOWNLOAD_ZIP_PATH = "partial_download_zip_path"
    }
    
    /**
     * Check if images have been successfully downloaded and extracted.
     */
    fun areImagesDownloaded(): Boolean {
        return prefs.getBoolean(KEY_IMAGES_DOWNLOADED, false)
    }
    
    /**
     * Get the version of the downloaded images.
     * @return The version string, or null if images haven't been downloaded
     */
    fun getDownloadedVersion(): String? {
        return prefs.getString(KEY_DOWNLOADED_VERSION, null)
    }
    
    /**
     * Get the timestamp when images were downloaded.
     * @return The timestamp in milliseconds, or null if images haven't been downloaded
     */
    fun getDownloadTimestamp(): Long? {
        val timestamp = prefs.getLong(KEY_DOWNLOAD_TIMESTAMP, -1L)
        return if (timestamp == -1L) null else timestamp
    }
    
    /**
     * Mark images as successfully downloaded and extracted.
     * 
     * @param version The version of the downloaded images
     * @param timestamp The timestamp when the download completed (defaults to current time)
     */
    fun markImagesAsDownloaded(version: String, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putBoolean(KEY_IMAGES_DOWNLOADED, true)
            .putString(KEY_DOWNLOADED_VERSION, version)
            .putLong(KEY_DOWNLOAD_TIMESTAMP, timestamp)
            .commit()  // Use commit() instead of apply() to ensure synchronous save
    }
    
    /**
     * Clear all download state, marking images as not downloaded.
     * This should be called when images are deleted.
     */
    fun clearDownloadState() {
        prefs.edit()
            .remove(KEY_IMAGES_DOWNLOADED)
            .remove(KEY_DOWNLOADED_VERSION)
            .remove(KEY_DOWNLOAD_TIMESTAMP)
            .apply()
    }
    
    /**
     * Save the current state of a partial download for resume functionality.
     * 
     * @param bytesDownloaded Number of bytes downloaded so far
     * @param totalBytes Total number of bytes to download
     * @param zipPath Path to the partially downloaded ZIP file
     */
    fun savePartialDownloadState(bytesDownloaded: Long, totalBytes: Long, zipPath: String) {
        prefs.edit()
            .putLong(KEY_PARTIAL_DOWNLOAD_BYTES, bytesDownloaded)
            .putLong(KEY_PARTIAL_DOWNLOAD_TOTAL_BYTES, totalBytes)
            .putString(KEY_PARTIAL_DOWNLOAD_ZIP_PATH, zipPath)
            .apply()
    }
    
    /**
     * Load the saved state of a partial download.
     * 
     * @return DownloadState if a partial download exists, null otherwise
     */
    fun loadPartialDownloadState(): DownloadState? {
        val bytesDownloaded = prefs.getLong(KEY_PARTIAL_DOWNLOAD_BYTES, -1L)
        val totalBytes = prefs.getLong(KEY_PARTIAL_DOWNLOAD_TOTAL_BYTES, -1L)
        val zipPath = prefs.getString(KEY_PARTIAL_DOWNLOAD_ZIP_PATH, null)
        
        return if (bytesDownloaded >= 0 && totalBytes > 0 && zipPath != null) {
            DownloadState(
                phase = DownloadPhase.DOWNLOADING,
                bytesDownloaded = bytesDownloaded,
                totalBytes = totalBytes,
                zipPath = zipPath
            )
        } else {
            null
        }
    }
    
    /**
     * Clear the saved state of a partial download.
     * This should be called when a download completes or is cancelled.
     */
    fun clearPartialDownloadState() {
        prefs.edit()
            .remove(KEY_PARTIAL_DOWNLOAD_BYTES)
            .remove(KEY_PARTIAL_DOWNLOAD_TOTAL_BYTES)
            .remove(KEY_PARTIAL_DOWNLOAD_ZIP_PATH)
            .apply()
    }
    
    /**
     * Clear all preferences (both download state and partial download state).
     * This is useful for testing or complete reset scenarios.
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
