package com.persona.companion.data.imagedownload

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service responsible for downloading ZIP files from a CDN using HTTP.
 * 
 * Supports:
 * - Progress tracking via callbacks
 * - HTTP range requests for resume functionality
 * - Configurable connection timeouts
 * - Proper error handling and cleanup
 * 
 * @property context Android context for accessing resources
 * @property cdnUrl The URL of the ZIP file to download
 */
class DownloadService(
    private val context: Context,
    private val cdnUrl: String
) {
    companion object {
        private const val TAG = "DownloadService"
        private const val CONNECTION_TIMEOUT_MS = 60_000 // 60 seconds
        private const val READ_TIMEOUT_MS = 60_000 // 60 seconds
        private const val BUFFER_SIZE = 8192 // 8 KB buffer for reading
    }
    
    /**
     * Downloads a ZIP file from the CDN to the specified destination.
     * 
     * @param destinationFile The file where the downloaded content will be saved
     * @param resumeFrom The byte position to resume from (0 for new download)
     * @param onProgress Callback invoked periodically with (bytesDownloaded, totalBytes)
     * @return Result containing the downloaded File on success, or DownloadError on failure
     */
    suspend fun downloadZip(
        destinationFile: File,
        resumeFrom: Long = 0,
        onProgress: (Long, Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        
        try {
            Log.d(TAG, "Starting download from $cdnUrl, resuming from byte $resumeFrom")
            
            // Check if we should attempt to resume
            val shouldResume = resumeFrom > 0 && destinationFile.exists()
            
            // Check if server supports range requests when resuming
            if (shouldResume && !supportsRangeRequests(cdnUrl)) {
                Log.w(TAG, "Server does not support range requests, starting from beginning")
                destinationFile.delete()
                return@withContext downloadZip(destinationFile, 0, onProgress)
            }
            
            // Create connection with appropriate range header
            connection = createConnection(cdnUrl, if (shouldResume) resumeFrom else 0)
            connection.connect()
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            // Validate response code
            when (responseCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL -> {
                    // Valid response, proceed with download
                }
                HttpURLConnection.HTTP_UNAVAILABLE -> {
                    return@withContext Result.failure(
                        Exception(DownloadError.NetworkError(
                            "Server unavailable",
                            IOException("HTTP $responseCode")
                        ).toUserMessage())
                    )
                }
                else -> {
                    return@withContext Result.failure(
                        Exception(DownloadError.NetworkError(
                            "HTTP error $responseCode",
                            IOException("HTTP $responseCode")
                        ).toUserMessage())
                    )
                }
            }
            
            // Get content length
            val contentLength = connection.contentLengthLong
            val totalBytes = if (shouldResume) {
                resumeFrom + contentLength
            } else {
                contentLength
            }
            
            if (totalBytes <= 0) {
                return@withContext Result.failure(
                    Exception(DownloadError.NetworkError(
                        "Invalid content length",
                        IOException("Content-Length: $totalBytes")
                    ).toUserMessage())
                )
            }
            
            Log.d(TAG, "Total bytes to download: $totalBytes, content length: $contentLength")
            
            // Open input stream from connection
            connection.inputStream.use { input ->
                // Open output stream (append if resuming, overwrite if new)
                FileOutputStream(destinationFile, shouldResume).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesDownloaded = resumeFrom
                    var bytesRead: Int
                    
                    // Report initial progress
                    onProgress(bytesDownloaded, totalBytes)
                    
                    // Read and write data in chunks
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead
                        
                        // Report progress
                        onProgress(bytesDownloaded, totalBytes)
                    }
                    
                    output.flush()
                }
            }
            
            Log.d(TAG, "Download completed successfully: ${destinationFile.absolutePath}")
            Result.success(destinationFile)
            
        } catch (e: IOException) {
            Log.e(TAG, "Download failed with IOException", e)
            
            // Clean up partial download if it's a new download (not a resume)
            if (resumeFrom == 0L && destinationFile.exists()) {
                destinationFile.delete()
            }
            
            Result.failure(
                Exception(DownloadError.NetworkError(
                    "Network error during download",
                    e
                ).toUserMessage())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Download failed with unexpected exception", e)
            
            // Clean up partial download if it's a new download
            if (resumeFrom == 0L && destinationFile.exists()) {
                destinationFile.delete()
            }
            
            Result.failure(
                Exception(DownloadError.NetworkError(
                    "Unexpected error during download",
                    e
                ).toUserMessage())
            )
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Checks if the server supports HTTP range requests by sending a HEAD request
     * and checking for the Accept-Ranges header.
     * 
     * @param url The URL to check
     * @return true if the server supports range requests, false otherwise
     */
    private fun supportsRangeRequests(url: String): Boolean {
        var connection: HttpURLConnection? = null
        
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = CONNECTION_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.connect()
            
            val acceptRanges = connection.getHeaderField("Accept-Ranges")
            val supportsRanges = acceptRanges != null && acceptRanges != "none"
            
            Log.d(TAG, "Server Accept-Ranges header: $acceptRanges, supports ranges: $supportsRanges")
            return supportsRanges
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check range request support, assuming not supported", e)
            return false
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Creates an HTTP connection with appropriate headers and timeouts.
     * 
     * @param url The URL to connect to
     * @param rangeStart The byte position to start from (0 for full download)
     * @return Configured HttpURLConnection
     */
    private fun createConnection(url: String, rangeStart: Long): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        
        // Set timeouts
        connection.connectTimeout = CONNECTION_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        
        // Set request method
        connection.requestMethod = "GET"
        
        // Add range header if resuming
        if (rangeStart > 0) {
            connection.setRequestProperty("Range", "bytes=$rangeStart-")
            Log.d(TAG, "Added Range header: bytes=$rangeStart-")
        }
        
        return connection
    }
}
