package com.persona.companion.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.StatFs
import android.util.Log
import com.persona.companion.BuildConfig
import com.persona.companion.data.imagedownload.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

/**
 * Main orchestrator for the cloud image download system.
 * 
 * This singleton manages the complete lifecycle of image downloads:
 * - Downloading ZIP files from CDN
 * - Extracting images to local storage
 * - Managing download state and persistence
 * - Handling errors and cleanup
 * - Providing status queries
 */
object ImageDownloadManager {
    private const val TAG = "ImageDownloadManager"
    private const val IMAGES_DIR_NAME = "images"
    private const val ZIP_FILE_NAME = "images.zip"
    private const val MIN_REQUIRED_SPACE_BYTES = 100_000_000L // 100 MB
    
    /**
     * Check if images have been successfully downloaded and extracted.
     */
    fun areImagesDownloaded(context: Context): Boolean {
        val prefs = ImageDownloadPreferences(context)
        val markedAsDownloaded = prefs.areImagesDownloaded()
        
        Log.d(TAG, "areImagesDownloaded: markedAsDownloaded=$markedAsDownloaded")
        
        // Verify files actually exist
        if (markedAsDownloaded) {
            // ZIP extracts to filesDir with "images/" folder inside
            val imagesDir = File(context.filesDir, "images")
            Log.d(TAG, "Checking images dir: ${imagesDir.absolutePath}")
            Log.d(TAG, "Images dir exists: ${imagesDir.exists()}")
            
            if (imagesDir.exists()) {
                val files = imagesDir.listFiles()
                Log.d(TAG, "Images dir file count: ${files?.size ?: 0}")
                files?.take(5)?.forEach { file ->
                    Log.d(TAG, "  - ${file.name} (isDir: ${file.isDirectory})")
                }
                
                // Check if we have the expected subdirectories
                val enemiesDir = File(imagesDir, "enemies_shared")
                val personasDir = File(imagesDir, "personas_shared")
                Log.d(TAG, "enemies_shared exists: ${enemiesDir.exists()}, has files: ${enemiesDir.listFiles()?.isNotEmpty()}")
                Log.d(TAG, "personas_shared exists: ${personasDir.exists()}, has files: ${personasDir.listFiles()?.isNotEmpty()}")
                
                // Only clear state if BOTH subdirectories are missing or empty
                if ((!enemiesDir.exists() || enemiesDir.listFiles()?.isEmpty() == true) &&
                    (!personasDir.exists() || personasDir.listFiles()?.isEmpty() == true)) {
                    Log.w(TAG, "Images marked as downloaded but subdirectories missing/empty, clearing state")
                    prefs.clearDownloadState()
                    return false
                }
            } else {
                // Images directory doesn't exist at all
                Log.w(TAG, "Images directory doesn't exist, clearing state")
                prefs.clearDownloadState()
                return false
            }
        }
        
        return markedAsDownloaded
    }
    
    /**
     * Get the current download status including version, timestamp, and storage size.
     */
    fun getDownloadStatus(context: Context): DownloadStatus {
        val prefs = ImageDownloadPreferences(context)
        val isDownloaded = areImagesDownloaded(context)
        
        return DownloadStatus(
            isDownloaded = isDownloaded,
            version = if (isDownloaded) prefs.getDownloadedVersion() else null,
            timestamp = if (isDownloaded) prefs.getDownloadTimestamp() else null,
            storageSize = if (isDownloaded) getStorageSize(context) else 0L
        )
    }
    
    /**
     * Get the total storage size consumed by downloaded images.
     */
    fun getStorageSize(context: Context): Long {
        // ZIP extracts to filesDir with "images/" folder inside
        val imagesDir = File(context.filesDir, "images")
        return if (imagesDir.exists()) {
            calculateDirectorySize(imagesDir)
        } else {
            0L
        }
    }
    
    /**
     * Start downloading images from the CDN.
     * 
     * This method orchestrates the complete download flow:
     * 1. Check available storage space
     * 2. Download ZIP file from CDN
     * 3. Validate ZIP file
     * 4. Extract images
     * 5. Clean up ZIP file
     * 6. Update state
     * 
     * @param context Android context
     * @param onProgress Callback for progress updates
     * @return Result containing Unit on success, or error message on failure
     */
    suspend fun startDownload(
        context: Context,
        onProgress: (DownloadProgress) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = ImageDownloadPreferences(context)
        
        try {
            // Phase 1: Check storage space
            onProgress(DownloadProgress(
                phase = DownloadPhase.CHECKING_STORAGE,
                bytesDownloaded = 0,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0
            ))
            
            if (!checkStorageSpace(context)) {
                val availableSpace = getAvailableSpace(context)
                val error = DownloadError.StorageError(
                    "Insufficient storage space",
                    availableSpace
                )
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error.toUserMessage()
                ))
                return@withContext Result.failure(Exception(error.toUserMessage()))
            }
            
            // Phase 2: Download ZIP
            val zipFile = File(context.cacheDir, ZIP_FILE_NAME)
            val downloadService = DownloadService(context, BuildConfig.IMAGES_CDN_URL)
            
            val downloadResult = downloadService.downloadZip(
                destinationFile = zipFile,
                resumeFrom = 0
            ) { bytesDownloaded, totalBytes ->
                onProgress(DownloadProgress(
                    phase = DownloadPhase.DOWNLOADING,
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes,
                    filesExtracted = 0,
                    totalFiles = 0
                ))
            }
            
            if (downloadResult.isFailure) {
                val error = downloadResult.exceptionOrNull()?.message ?: "Download failed"
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error
                ))
                return@withContext Result.failure(Exception(error))
            }
            
            // Phase 3: Validate ZIP
            val extractionService = ExtractionService(context)
            if (!extractionService.validateZipFile(zipFile, BuildConfig.IMAGES_ZIP_SIZE)) {
                zipFile.delete()
                val error = DownloadError.IntegrityError(
                    "ZIP file validation failed",
                    BuildConfig.IMAGES_ZIP_SIZE,
                    zipFile.length()
                )
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error.toUserMessage()
                ))
                return@withContext Result.failure(Exception(error.toUserMessage()))
            }
            
            // Phase 4: Extract ZIP
            // Extract directly to filesDir since ZIP already contains "images/" folder
            val extractionResult = extractionService.extractZip(
                zipFile = zipFile,
                destinationDir = context.filesDir
            ) { filesExtracted, totalFiles ->
                onProgress(DownloadProgress(
                    phase = DownloadPhase.EXTRACTING,
                    bytesDownloaded = BuildConfig.IMAGES_ZIP_SIZE,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = filesExtracted,
                    totalFiles = totalFiles
                ))
            }
            
            if (extractionResult.isFailure) {
                val error = extractionResult.exceptionOrNull()?.message ?: "Extraction failed"
                zipFile.delete()
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error
                ))
                return@withContext Result.failure(Exception(error))
            }
            
            // Phase 5: Clean up ZIP file
            zipFile.delete()
            
            // Phase 6: Update state
            prefs.markImagesAsDownloaded(BuildConfig.IMAGES_VERSION)
            prefs.clearPartialDownloadState()
            
            // Phase 7: Complete
            onProgress(DownloadProgress(
                phase = DownloadPhase.COMPLETE,
                bytesDownloaded = BuildConfig.IMAGES_ZIP_SIZE,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0
            ))
            
            Log.d(TAG, "Image download completed successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Image download failed", e)
            cleanupAfterError(context, DownloadError.StateError(e.message ?: "Unknown error"))
            onProgress(DownloadProgress(
                phase = DownloadPhase.ERROR,
                bytesDownloaded = 0,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0,
                error = e.message ?: "Unknown error"
            ))
            Result.failure(e)
        }
    }
    
    /**
     * Resume a previously interrupted download.
     * 
     * Loads the saved download state and attempts to resume from the last position.
     * Falls back to a fresh download if resume is not supported.
     * 
     * @param context Android context
     * @param onProgress Callback for progress updates
     * @return Result containing Unit on success, or error message on failure
     */
    suspend fun resumeDownload(
        context: Context,
        onProgress: (DownloadProgress) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = ImageDownloadPreferences(context)
        val savedState = prefs.loadPartialDownloadState()
        
        if (savedState == null) {
            Log.w(TAG, "No saved download state found, starting fresh download")
            return@withContext startDownload(context, onProgress)
        }
        
        try {
            val zipFile = File(savedState.zipPath ?: return@withContext startDownload(context, onProgress))
            
            if (!zipFile.exists()) {
                Log.w(TAG, "Partial download file not found, starting fresh download")
                prefs.clearPartialDownloadState()
                return@withContext startDownload(context, onProgress)
            }
            
            // Resume download
            val downloadService = DownloadService(context, BuildConfig.IMAGES_CDN_URL)
            val downloadResult = downloadService.downloadZip(
                destinationFile = zipFile,
                resumeFrom = savedState.bytesDownloaded
            ) { bytesDownloaded, totalBytes ->
                // Save progress periodically
                if (bytesDownloaded % 1_000_000 == 0L) { // Every 1 MB
                    prefs.savePartialDownloadState(bytesDownloaded, totalBytes, zipFile.absolutePath)
                }
                
                onProgress(DownloadProgress(
                    phase = DownloadPhase.DOWNLOADING,
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes,
                    filesExtracted = 0,
                    totalFiles = 0
                ))
            }
            
            if (downloadResult.isFailure) {
                val error = downloadResult.exceptionOrNull()?.message ?: "Download failed"
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error
                ))
                return@withContext Result.failure(Exception(error))
            }
            
            // Continue with extraction (same as startDownload)
            val extractionService = ExtractionService(context)
            if (!extractionService.validateZipFile(zipFile, BuildConfig.IMAGES_ZIP_SIZE)) {
                zipFile.delete()
                prefs.clearPartialDownloadState()
                val error = DownloadError.IntegrityError(
                    "ZIP file validation failed",
                    BuildConfig.IMAGES_ZIP_SIZE,
                    zipFile.length()
                )
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error.toUserMessage()
                ))
                return@withContext Result.failure(Exception(error.toUserMessage()))
            }
            
            // Extract directly to filesDir since ZIP already contains "images/" folder
            val extractionResult = extractionService.extractZip(
                zipFile = zipFile,
                destinationDir = context.filesDir
            ) { filesExtracted, totalFiles ->
                onProgress(DownloadProgress(
                    phase = DownloadPhase.EXTRACTING,
                    bytesDownloaded = BuildConfig.IMAGES_ZIP_SIZE,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = filesExtracted,
                    totalFiles = totalFiles
                ))
            }
            
            if (extractionResult.isFailure) {
                val error = extractionResult.exceptionOrNull()?.message ?: "Extraction failed"
                zipFile.delete()
                prefs.clearPartialDownloadState()
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error
                ))
                return@withContext Result.failure(Exception(error))
            }
            
            // Clean up and update state
            zipFile.delete()
            prefs.markImagesAsDownloaded(BuildConfig.IMAGES_VERSION)
            prefs.clearPartialDownloadState()
            
            onProgress(DownloadProgress(
                phase = DownloadPhase.COMPLETE,
                bytesDownloaded = BuildConfig.IMAGES_ZIP_SIZE,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0
            ))
            
            Log.d(TAG, "Image download resumed and completed successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Resume download failed", e)
            prefs.clearPartialDownloadState()
            onProgress(DownloadProgress(
                phase = DownloadPhase.ERROR,
                bytesDownloaded = 0,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0,
                error = e.message ?: "Unknown error"
            ))
            Result.failure(e)
        }
    }
    
    /**
     * Delete all downloaded images and clear state.
     * 
     * @param context Android context
     * @return Result containing the amount of space freed in bytes, or error on failure
     */
    suspend fun deleteImages(context: Context): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // ZIP extracts to filesDir with "images/" folder inside
            val imagesDir = File(context.filesDir, "images")
            val sizeFreed = if (imagesDir.exists()) {
                val size = calculateDirectorySize(imagesDir)
                imagesDir.deleteRecursively()
                size
            } else {
                0L
            }
            
            // Clear state
            val prefs = ImageDownloadPreferences(context)
            prefs.clearDownloadState()
            prefs.clearPartialDownloadState()
            
            Log.d(TAG, "Deleted images, freed $sizeFreed bytes")
            Result.success(sizeFreed)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete images", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if the device is on a metered connection (cellular data, hotspot, etc.).
     */
    fun isMeteredConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            // Check if connection is metered
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.isActiveNetworkMetered
        }
    }
    
    /**
     * Check if there's enough storage space for download and extraction.
     */
    private fun checkStorageSpace(context: Context): Boolean {
        val availableSpace = getAvailableSpace(context)
        return availableSpace >= MIN_REQUIRED_SPACE_BYTES
    }
    
    /**
     * Get available storage space in bytes.
     */
    private fun getAvailableSpace(context: Context): Long {
        val stat = StatFs(context.filesDir.absolutePath)
        return stat.availableBlocksLong * stat.blockSizeLong
    }
    
    /**
     * Calculate the total size of a directory recursively.
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        
        return size
    }
    
    /**
     * Clean up after an error based on the error type.
     */
    private suspend fun cleanupAfterError(context: Context, error: DownloadError) = withContext(Dispatchers.IO) {
        val prefs = ImageDownloadPreferences(context)
        
        when (error) {
            is DownloadError.NetworkError -> {
                // Keep partial download for resume if > 10% complete
                val savedState = prefs.loadPartialDownloadState()
                if (savedState != null && savedState.bytesDownloaded > savedState.totalBytes * 0.1) {
                    Log.d(TAG, "Keeping partial download for resume")
                } else {
                    // Delete partial download
                    val zipFile = File(context.cacheDir, ZIP_FILE_NAME)
                    zipFile.delete()
                    prefs.clearPartialDownloadState()
                }
            }
            is DownloadError.StorageError -> {
                // Delete partial download
                val zipFile = File(context.cacheDir, ZIP_FILE_NAME)
                zipFile.delete()
                prefs.clearPartialDownloadState()
            }
            is DownloadError.IntegrityError -> {
                // Delete corrupted files
                val zipFile = File(context.cacheDir, ZIP_FILE_NAME)
                zipFile.delete()
                // ZIP extracts to filesDir with "images/" folder inside
                val imagesDir = File(context.filesDir, "images")
                imagesDir.deleteRecursively()
                prefs.clearDownloadState()
                prefs.clearPartialDownloadState()
            }
            is DownloadError.StateError -> {
                // Reconcile state
                // ZIP extracts to filesDir with "images/" folder inside
                val imagesDir = File(context.filesDir, "images")
                if (!imagesDir.exists() || imagesDir.listFiles()?.isEmpty() == true) {
                    prefs.clearDownloadState()
                } else {
                    // State is consistent, no action needed
                }
            }
        }
    }
    
    /**
     * Import images from a local ZIP file selected by the user.
     * 
     * @param context Android context
     * @param uri URI of the selected ZIP file
     * @param onProgress Callback for progress updates
     * @return Result containing Unit on success, or error message on failure
     */
    suspend fun importFromUri(
        context: Context,
        uri: android.net.Uri,
        onProgress: (DownloadProgress) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = ImageDownloadPreferences(context)
        
        try {
            // Phase 1: Check storage space
            onProgress(DownloadProgress(
                phase = DownloadPhase.CHECKING_STORAGE,
                bytesDownloaded = 0,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0
            ))
            
            if (!checkStorageSpace(context)) {
                val error = DownloadError.StorageError(
                    "Insufficient storage space",
                    getAvailableSpace(context)
                )
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error.toUserMessage()
                ))
                return@withContext Result.failure(Exception(error.toUserMessage()))
            }
            
            // Phase 2: Copy ZIP file to cache
            val zipFile = File(context.cacheDir, ZIP_FILE_NAME)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Failed to open file"))
            
            inputStream.use { input ->
                zipFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // Update progress
                        onProgress(DownloadProgress(
                            phase = DownloadPhase.DOWNLOADING,
                            bytesDownloaded = totalBytesRead,
                            totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                            filesExtracted = 0,
                            totalFiles = 0
                        ))
                    }
                }
            }
            
            // Phase 3: Validate ZIP file (lenient size check for imports)
            val extractionService = ExtractionService(context)
            val actualSize = zipFile.length()
            
            Log.d(TAG, "ZIP file size: $actualSize bytes")
            
            // For imports, allow size to be within 10% of expected size
            val minSize = (BuildConfig.IMAGES_ZIP_SIZE * 0.9).toLong()
            val maxSize = (BuildConfig.IMAGES_ZIP_SIZE * 1.1).toLong()
            
            if (actualSize < minSize || actualSize > maxSize) {
                zipFile.delete()
                val error = DownloadError.IntegrityError(
                    "ZIP file size is incorrect (expected ~${BuildConfig.IMAGES_ZIP_SIZE / 1_000_000} MB, got ${actualSize / 1_000_000} MB)",
                    BuildConfig.IMAGES_ZIP_SIZE,
                    actualSize
                )
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error.toUserMessage()
                ))
                return@withContext Result.failure(Exception(error.toUserMessage()))
            }
            
            // Validate ZIP format (can be opened and read)
            try {
                java.util.zip.ZipFile(zipFile).use { zip ->
                    val entries = zip.entries().toList()
                    val entryCount = entries.size
                    if (entryCount == 0) {
                        throw Exception("ZIP file is empty")
                    }
                    Log.d(TAG, "ZIP file is valid with $entryCount entries")
                    Log.d(TAG, "First 5 entries:")
                    entries.take(5).forEach { entry ->
                        Log.d(TAG, "  - ${entry.name} (size: ${entry.size}, isDir: ${entry.isDirectory})")
                    }
                }
            } catch (e: Exception) {
                zipFile.delete()
                val error = DownloadError.IntegrityError(
                    "ZIP file is corrupted or invalid: ${e.message}",
                    BuildConfig.IMAGES_ZIP_SIZE,
                    actualSize
                )
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error.toUserMessage()
                ))
                return@withContext Result.failure(Exception(error.toUserMessage()))
            }
            
            // Phase 4: Extract images
            // Extract directly to filesDir since ZIP already contains "images/" folder
            Log.d(TAG, "Starting extraction to: ${context.filesDir.absolutePath}")
            val extractionResult = extractionService.extractZip(
                zipFile = zipFile,
                destinationDir = context.filesDir
            ) { filesExtracted, totalFiles ->
                Log.d(TAG, "Extraction progress: $filesExtracted / $totalFiles")
                onProgress(DownloadProgress(
                    phase = DownloadPhase.EXTRACTING,
                    bytesDownloaded = BuildConfig.IMAGES_ZIP_SIZE,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = filesExtracted,
                    totalFiles = totalFiles
                ))
            }
            
            if (extractionResult.isFailure) {
                val error = extractionResult.exceptionOrNull()?.message ?: "Extraction failed"
                Log.e(TAG, "Extraction failed: $error")
                zipFile.delete()
                onProgress(DownloadProgress(
                    phase = DownloadPhase.ERROR,
                    bytesDownloaded = 0,
                    totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                    filesExtracted = 0,
                    totalFiles = 0,
                    error = error
                ))
                return@withContext Result.failure(Exception(error))
            }
            
            Log.d(TAG, "Extraction completed successfully")
            
            // Verify extraction
            val imagesDir = File(context.filesDir, "images")
            Log.d(TAG, "Verifying extraction at: ${imagesDir.absolutePath}")
            Log.d(TAG, "Images dir exists: ${imagesDir.exists()}")
            if (imagesDir.exists()) {
                val files = imagesDir.listFiles()
                Log.d(TAG, "Images dir contains ${files?.size ?: 0} items")
                files?.take(5)?.forEach { file ->
                    Log.d(TAG, "  - ${file.name} (isDir: ${file.isDirectory})")
                }
            }
            
            // Phase 5: Clean up ZIP file
            zipFile.delete()
            
            // Phase 6: Update state
            Log.d(TAG, "Import - Marking images as downloaded, version: ${BuildConfig.IMAGES_VERSION}")
            prefs.markImagesAsDownloaded(BuildConfig.IMAGES_VERSION)
            prefs.clearPartialDownloadState()
            
            // Verify state was saved
            Log.d(TAG, "Import - Verification - areImagesDownloaded: ${prefs.areImagesDownloaded()}")
            Log.d(TAG, "Import - Verification - version: ${prefs.getDownloadedVersion()}")
            
            // Write debug info to a file
            try {
                val debugFile = File(context.getExternalFilesDir(null), "import_debug.txt")
                debugFile.writeText("""
                    Import completed at: ${System.currentTimeMillis()}
                    Prefs marked as downloaded: ${prefs.areImagesDownloaded()}
                    Version: ${prefs.getDownloadedVersion()}
                    
                    Images dir: ${File(context.filesDir, "images").absolutePath}
                    Images dir exists: ${File(context.filesDir, "images").exists()}
                    
                    Subdirectories:
                    ${File(context.filesDir, "images").listFiles()?.joinToString("\n") { "  - ${it.name} (isDir: ${it.isDirectory}, files: ${it.listFiles()?.size ?: 0})" } ?: "No files"}
                    
                    enemies_shared: ${File(context.filesDir, "images/enemies_shared").exists()} (${File(context.filesDir, "images/enemies_shared").listFiles()?.size ?: 0} files)
                    personas_shared: ${File(context.filesDir, "images/personas_shared").exists()} (${File(context.filesDir, "images/personas_shared").listFiles()?.size ?: 0} files)
                """.trimIndent())
                Log.d(TAG, "Debug info written to: ${debugFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write debug file", e)
            }
            
            // Phase 7: Complete
            onProgress(DownloadProgress(
                phase = DownloadPhase.COMPLETE,
                bytesDownloaded = BuildConfig.IMAGES_ZIP_SIZE,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0
            ))
            
            Log.d(TAG, "Image import completed successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Image import failed", e)
            onProgress(DownloadProgress(
                phase = DownloadPhase.ERROR,
                bytesDownloaded = 0,
                totalBytes = BuildConfig.IMAGES_ZIP_SIZE,
                filesExtracted = 0,
                totalFiles = 0,
                error = e.message ?: "Unknown error"
            ))
            Result.failure(e)
        }
    }
}
