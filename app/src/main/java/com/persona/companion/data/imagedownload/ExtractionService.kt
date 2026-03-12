package com.persona.companion.data.imagedownload

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipException

/**
 * Service responsible for extracting ZIP files and validating their integrity.
 * 
 * Supports:
 * - ZIP extraction maintaining directory structure
 * - Progress tracking via callbacks
 * - File size and format validation
 * - Cleanup of partial or corrupted extractions
 * 
 * @property context Android context for accessing resources
 */
class ExtractionService(private val context: Context) {
    companion object {
        private const val TAG = "ExtractionService"
        private const val BUFFER_SIZE = 8192 // 8 KB buffer for reading
    }
    
    /**
     * Extracts a ZIP file to the specified destination directory.
     * 
     * The extraction maintains the directory structure from the ZIP file.
     * Progress is reported via the onProgress callback with (filesExtracted, totalFiles).
     * 
     * @param zipFile The ZIP file to extract
     * @param destinationDir The directory where files will be extracted
     * @param onProgress Callback invoked periodically with (filesExtracted, totalFiles)
     * @return Result containing Unit on success, or DownloadError on failure
     */
    suspend fun extractZip(
        zipFile: File,
        destinationDir: File,
        onProgress: (Int, Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var zipFileHandle: ZipFile? = null
        
        try {
            Log.d(TAG, "Starting extraction of ${zipFile.absolutePath} to ${destinationDir.absolutePath}")
            
            // Ensure destination directory exists
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }
            
            // Open ZIP file
            zipFileHandle = ZipFile(zipFile)
            
            // Get all entries
            val entries = zipFileHandle.entries().toList()
            val totalFiles = entries.size
            
            Log.d(TAG, "ZIP contains $totalFiles entries")
            
            if (totalFiles == 0) {
                return@withContext Result.failure(
                    Exception(DownloadError.IntegrityError(
                        "ZIP file is empty",
                        expectedSize = 0,
                        actualSize = 0
                    ).toUserMessage())
                )
            }
            
            var filesExtracted = 0
            
            // Report initial progress
            onProgress(filesExtracted, totalFiles)
            
            // Extract each entry
            for (entry in entries) {
                extractEntry(zipFileHandle, entry, destinationDir)
                filesExtracted++
                
                // Report progress
                onProgress(filesExtracted, totalFiles)
            }
            
            Log.d(TAG, "Extraction completed successfully: $filesExtracted files extracted")
            Result.success(Unit)
            
        } catch (e: ZipException) {
            Log.e(TAG, "ZIP extraction failed with ZipException", e)
            
            // Clean up partial extraction
            cleanupPartialExtraction(destinationDir)
            
            Result.failure(
                Exception(DownloadError.IntegrityError(
                    "ZIP file is corrupted or invalid",
                    expectedSize = zipFile.length(),
                    actualSize = zipFile.length()
                ).toUserMessage())
            )
        } catch (e: IOException) {
            Log.e(TAG, "ZIP extraction failed with IOException", e)
            
            // Clean up partial extraction
            cleanupPartialExtraction(destinationDir)
            
            Result.failure(
                Exception(DownloadError.StorageError(
                    "I/O error during extraction",
                    availableSpace = destinationDir.usableSpace
                ).toUserMessage())
            )
        } catch (e: Exception) {
            Log.e(TAG, "ZIP extraction failed with unexpected exception", e)
            
            // Clean up partial extraction
            cleanupPartialExtraction(destinationDir)
            
            Result.failure(
                Exception(DownloadError.StateError(
                    "Unexpected error during extraction"
                ).toUserMessage())
            )
        } finally {
            zipFileHandle?.close()
        }
    }
    
    /**
     * Extracts a single ZIP entry to the destination directory.
     * 
     * Maintains the directory structure and handles both files and directories.
     * Validates that the entry path doesn't escape the destination directory (zip slip protection).
     * 
     * @param zipFile The ZipFile handle
     * @param entry The ZIP entry to extract
     * @param destinationDir The destination directory
     * @throws IOException if extraction fails
     * @throws SecurityException if the entry path attempts to escape the destination directory
     */
    private fun extractEntry(zipFile: ZipFile, entry: ZipEntry, destinationDir: File) {
        // Normalize path separators (convert backslashes to forward slashes)
        val entryName = entry.name.replace('\\', '/')
        
        // Create the destination file
        val destFile = File(destinationDir, entryName)
        
        // Security check: prevent zip slip vulnerability
        // Ensure the canonical path of the destination file is within the destination directory
        val destDirCanonical = destinationDir.canonicalPath
        val destFileCanonical = destFile.canonicalPath
        
        if (!destFileCanonical.startsWith(destDirCanonical + File.separator)) {
            throw SecurityException("Entry is outside of the target directory: $entryName")
        }
        
        if (entry.isDirectory) {
            // Create directory
            if (!destFile.exists()) {
                destFile.mkdirs()
            }
        } else {
            // Create parent directories if they don't exist
            destFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
            
            // Extract file
            zipFile.getInputStream(entry).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    
                    output.flush()
                }
            }
        }
    }
    
    /**
     * Validates a ZIP file by checking its size and format.
     * 
     * Performs the following checks:
     * - File exists and is readable
     * - File size matches the expected size
     * - File is a valid ZIP format (can be opened as ZipFile)
     * 
     * @param zipFile The ZIP file to validate
     * @param expectedSize The expected file size in bytes
     * @return true if the ZIP file is valid, false otherwise
     */
    fun validateZipFile(zipFile: File, expectedSize: Long): Boolean {
        try {
            // Check if file exists
            if (!zipFile.exists()) {
                Log.e(TAG, "ZIP file does not exist: ${zipFile.absolutePath}")
                return false
            }
            
            // Check if file is readable
            if (!zipFile.canRead()) {
                Log.e(TAG, "ZIP file is not readable: ${zipFile.absolutePath}")
                return false
            }
            
            // Check file size
            val actualSize = zipFile.length()
            if (actualSize != expectedSize) {
                Log.e(TAG, "ZIP file size mismatch: expected $expectedSize, got $actualSize")
                return false
            }
            
            // Check if file is a valid ZIP
            ZipFile(zipFile).use { zip ->
                // Try to get the entries to verify it's a valid ZIP
                val entryCount = zip.entries().toList().size
                Log.d(TAG, "ZIP file is valid with $entryCount entries")
            }
            
            return true
            
        } catch (e: ZipException) {
            Log.e(TAG, "ZIP file is corrupted or invalid", e)
            return false
        } catch (e: IOException) {
            Log.e(TAG, "I/O error while validating ZIP file", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while validating ZIP file", e)
            return false
        }
    }
    
    /**
     * Cleans up a partial extraction by recursively deleting all files and subdirectories
     * in the destination directory.
     * 
     * This is called when extraction fails to ensure no partial or corrupted files remain.
     * 
     * @param destinationDir The directory to clean up
     */
    private fun cleanupPartialExtraction(destinationDir: File) {
        try {
            if (destinationDir.exists()) {
                Log.d(TAG, "Cleaning up partial extraction: ${destinationDir.absolutePath}")
                
                val deleted = destinationDir.deleteRecursively()
                
                if (deleted) {
                    Log.d(TAG, "Partial extraction cleaned up successfully")
                } else {
                    Log.w(TAG, "Failed to clean up partial extraction")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup of partial extraction", e)
        }
    }
}
