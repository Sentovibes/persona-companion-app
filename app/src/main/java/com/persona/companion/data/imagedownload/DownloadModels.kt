package com.persona.companion.data.imagedownload

/**
 * Represents the current status of the image download system.
 *
 * @property isDownloaded Whether images have been successfully downloaded and extracted
 * @property version The version of the downloaded images (null if not downloaded)
 * @property timestamp The timestamp when images were downloaded (null if not downloaded)
 * @property storageSize The total size in bytes consumed by downloaded images
 */
data class DownloadStatus(
    val isDownloaded: Boolean,
    val version: String?,
    val timestamp: Long?,
    val storageSize: Long
)

/**
 * Represents the progress of an ongoing download or extraction operation.
 *
 * @property phase The current phase of the operation
 * @property bytesDownloaded Number of bytes downloaded so far
 * @property totalBytes Total number of bytes to download
 * @property filesExtracted Number of files extracted so far
 * @property totalFiles Total number of files to extract
 * @property error Error message if the operation failed (null if no error)
 */
data class DownloadProgress(
    val phase: DownloadPhase,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val filesExtracted: Int,
    val totalFiles: Int,
    val error: String? = null
)

/**
 * Represents the different phases of the download and extraction process.
 */
enum class DownloadPhase {
    /** Checking available storage space before download */
    CHECKING_STORAGE,
    
    /** Downloading the ZIP file from CDN */
    DOWNLOADING,
    
    /** Extracting the downloaded ZIP file */
    EXTRACTING,
    
    /** Operation completed successfully */
    COMPLETE,
    
    /** Operation failed with an error */
    ERROR
}

/**
 * Represents the persistent state of a download operation.
 * Used for resuming interrupted downloads.
 *
 * @property phase The phase when the download was interrupted
 * @property bytesDownloaded Number of bytes downloaded before interruption
 * @property totalBytes Total number of bytes to download
 * @property zipPath Path to the partially downloaded ZIP file (null if not started)
 */
data class DownloadState(
    val phase: DownloadPhase,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val zipPath: String?
)

/**
 * Sealed class representing different types of errors that can occur during download/extraction.
 */
sealed class DownloadError {
    /**
     * Network-related errors (connection failures, timeouts, CDN unavailability).
     *
     * @property message Human-readable error message
     * @property cause The underlying exception that caused the error (if any)
     */
    data class NetworkError(val message: String, val cause: Throwable? = null) : DownloadError()
    
    /**
     * Storage-related errors (insufficient space, permission issues, I/O failures).
     *
     * @property message Human-readable error message
     * @property availableSpace The amount of available storage space in bytes
     */
    data class StorageError(val message: String, val availableSpace: Long) : DownloadError()
    
    /**
     * Data integrity errors (corrupted ZIP, size mismatches, invalid file format).
     *
     * @property message Human-readable error message
     * @property expectedSize The expected file size in bytes
     * @property actualSize The actual file size in bytes
     */
    data class IntegrityError(
        val message: String,
        val expectedSize: Long,
        val actualSize: Long
    ) : DownloadError()
    
    /**
     * State errors (inconsistent state between SharedPreferences and file system).
     *
     * @property message Human-readable error message
     */
    data class StateError(val message: String) : DownloadError()
}

/**
 * Extension function to convert a DownloadError to a user-friendly message.
 */
fun DownloadError.toUserMessage(): String = when (this) {
    is DownloadError.NetworkError -> "Network error. Check your connection and try again."
    is DownloadError.StorageError -> "Not enough storage space. Free up space and try again."
    is DownloadError.IntegrityError -> "Download corrupted. Please try again."
    is DownloadError.StateError -> "An error occurred. Please try again."
}
