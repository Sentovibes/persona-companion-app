package com.persona.companion.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

class ImageDownloadManager(private val context: Context) {
    
    private val imageDir = File(context.filesDir, "images/personas")
    private val downloadUrl = "https://github.com/Sentovibes/persona-companion-app/releases/download/images-v1.0/persona-images-hd-v1.0.zip"
    
    init {
        imageDir.mkdirs()
    }
    
    /**
     * Check if HD personas are downloaded
     */
    fun arePersonasDownloaded(): Boolean {
        return imageDir.exists() && imageDir.listFiles()?.isNotEmpty() == true
    }
    
    /**
     * Download images from GitHub
     */
    suspend fun downloadImages(onProgress: (Float) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("ImageDownload", "Starting download from $downloadUrl")
            
            val connection = URL(downloadUrl).openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val totalSize = connection.contentLength
            val inputStream = connection.getInputStream()
            
            // Download to temp file
            val tempFile = File(context.cacheDir, "images_temp.zip")
            val outputStream = FileOutputStream(tempFile)
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (totalSize > 0) {
                    onProgress(totalBytesRead.toFloat() / totalSize)
                }
            }
            
            outputStream.close()
            inputStream.close()
            
            Log.d("ImageDownload", "Download complete, extracting...")
            
            // Extract zip
            extractZip(tempFile, onProgress)
            
            // Clean up
            tempFile.delete()
            
            Log.d("ImageDownload", "Extraction complete")
            true
        } catch (e: Exception) {
            Log.e("ImageDownload", "Download failed", e)
            false
        }
    }
    
    /**
     * Import images from user-selected zip file
     */
    suspend fun importFromFile(uri: Uri, onProgress: (Float) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("ImageDownload", "Importing from file: $uri")
            
            val tempFile = File(context.cacheDir, "images_import.zip")
            
            // Copy URI to temp file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Extract zip
            extractZip(tempFile, onProgress)
            
            // Clean up
            tempFile.delete()
            
            Log.d("ImageDownload", "Import complete")
            true
        } catch (e: Exception) {
            Log.e("ImageDownload", "Import failed", e)
            false
        }
    }
    
    /**
     * Extract zip file to image directory
     */
    private fun extractZip(zipFile: File, onProgress: (Float) -> Unit) {
        // Clear existing images
        imageDir.deleteRecursively()
        imageDir.mkdirs()
        
        ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            var count = 0
            
            while (entry != null) {
                if (!entry.isDirectory) {
                    val fileName = entry.name.substringAfterLast("/")
                    val outputFile = File(imageDir, fileName)
                    
                    FileOutputStream(outputFile).use { output ->
                        zip.copyTo(output)
                    }
                    
                    count++
                    if (count % 10 == 0) {
                        onProgress(0.5f + (count / 640f) * 0.5f) // 50-100% for extraction
                    }
                }
                
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }
    
    /**
     * Delete downloaded images
     */
    fun deleteImages() {
        imageDir.deleteRecursively()
        Log.d("ImageDownload", "Images deleted")
    }
    
    /**
     * Get image file if it exists
     */
    fun getImageFile(personaName: String): File? {
        val safeName = personaName.replace("/", "_").replace(":", "").replace("?", "")
        val file = File(imageDir, "$safeName.png")
        return if (file.exists()) file else null
    }
}
