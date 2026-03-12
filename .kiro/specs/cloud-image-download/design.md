# Design Document: Cloud Image Download System

## Overview

The cloud image download system transforms the Persona Companion app from bundling all images in the APK (~50MB) to downloading them on-demand from a CDN, reducing the base APK size to ~5MB. The system uses a single ZIP file approach rather than individual image downloads, providing simplicity and full offline access once downloaded.

### Key Design Decisions

1. **Single ZIP vs Individual Downloads**: We chose a single ZIP file containing all images rather than downloading images individually. This simplifies the implementation, reduces HTTP overhead, provides atomic download/extraction operations, and ensures all images are available offline once downloaded.

2. **CDN Selection**: GitHub Releases provides free hosting for open-source projects with generous bandwidth limits. Alternative options include Cloudflare R2 (free tier: 10GB storage, 10M requests/month) or Firebase Storage. The CDN URL will be configurable via build configuration to allow easy migration.

3. **Storage Location**: Images will be stored in the app's internal storage (`context.filesDir/images/`) rather than external storage. This avoids permission requests, ensures data privacy, and automatically cleans up on app uninstall.

4. **Extraction Strategy**: The ZIP will be extracted maintaining the directory structure (personas/p3/, enemies/p5r/, etc.) to match the existing image path conventions used throughout the app.

5. **Resume Support**: HTTP range requests will be used for download resumption. If the CDN doesn't support range requests, downloads will restart from the beginning (acceptable tradeoff for simplicity).

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Settings UI                           │
│  (SettingsScreen with download controls & progress display) │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                  ImageDownloadManager                        │
│  - Download orchestration                                    │
│  - Progress tracking                                         │
│  - State persistence                                         │
│  - Error handling                                            │
└────┬──────────────────────────────────┬─────────────────────┘
     │                                   │
     ▼                                   ▼
┌─────────────────────┐      ┌──────────────────────────────┐
│  DownloadService    │      │   ExtractionService          │
│  - HTTP download    │      │   - ZIP extraction           │
│  - Range requests   │      │   - File validation          │
│  - Progress events  │      │   - Cleanup                  │
└──────────┬──────────┘      └──────────┬───────────────────┘
           │                             │
           ▼                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    Storage Layer                             │
│  - SharedPreferences (download state)                        │
│  - Internal storage (images/)                                │
│  - Cache directory (temporary ZIP)                           │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Image Loader                              │
│  - Path resolution                                           │
│  - Fallback to placeholder                                   │
│  - Memory caching (Coil library)                             │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

**Download Flow:**
1. User taps "Download Images" in Settings
2. SettingsScreen calls `ImageDownloadManager.startDownload()`
3. ImageDownloadManager checks storage space and network type
4. DownloadService downloads ZIP to cache directory with progress callbacks
5. ExtractionService extracts ZIP to internal storage
6. ImageDownloadManager updates SharedPreferences with completion status
7. SettingsScreen updates UI to show "Downloaded" state

**Image Loading Flow:**
1. UI component requests image (e.g., persona "Arsene" from P5)
2. ImageLoader constructs path: "images/personas/p5/arsene.webp"
3. ImageLoader checks if file exists in internal storage
4. If exists: load and cache in memory
5. If not exists: display placeholder image

**Deletion Flow:**
1. User taps "Delete Images" and confirms
2. ImageDownloadManager recursively deletes images directory
3. ImageDownloadManager clears SharedPreferences
4. SettingsScreen updates UI to show "Not Downloaded" state

## Components and Interfaces

### ImageDownloadManager

The central orchestrator for all download operations.

```kotlin
object ImageDownloadManager {
    // State queries
    fun areImagesDownloaded(context: Context): Boolean
    fun getDownloadStatus(context: Context): DownloadStatus
    fun getStorageSize(context: Context): Long
    
    // Download operations
    suspend fun startDownload(
        context: Context,
        onProgress: (DownloadProgress) -> Unit
    ): Result<Unit>
    
    suspend fun resumeDownload(
        context: Context,
        onProgress: (DownloadProgress) -> Unit
    ): Result<Unit>
    
    suspend fun cancelDownload()
    
    // Storage management
    suspend fun deleteImages(context: Context): Result<Long>
    
    // Internal helpers
    private suspend fun checkStorageSpace(context: Context): Boolean
    private suspend fun isMeteredConnection(context: Context): Boolean
    private fun saveDownloadState(context: Context, state: DownloadState)
    private fun loadDownloadState(context: Context): DownloadState?
}

data class DownloadStatus(
    val isDownloaded: Boolean,
    val version: String?,
    val timestamp: Long?,
    val storageSize: Long
)

data class DownloadProgress(
    val phase: DownloadPhase,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val filesExtracted: Int,
    val totalFiles: Int,
    val error: String?
)

enum class DownloadPhase {
    CHECKING_STORAGE,
    DOWNLOADING,
    EXTRACTING,
    COMPLETE,
    ERROR
}

data class DownloadState(
    val phase: DownloadPhase,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val zipPath: String?
)
```

### DownloadService

Handles HTTP download with progress tracking and resume support.

```kotlin
class DownloadService(
    private val context: Context,
    private val cdnUrl: String
) {
    suspend fun downloadZip(
        destinationFile: File,
        resumeFrom: Long = 0,
        onProgress: (Long, Long) -> Unit
    ): Result<File>
    
    private fun supportsRangeRequests(url: String): Boolean
    private fun createConnection(url: String, rangeStart: Long): HttpURLConnection
}
```

### ExtractionService

Handles ZIP extraction with validation and cleanup.

```kotlin
class ExtractionService(private val context: Context) {
    suspend fun extractZip(
        zipFile: File,
        destinationDir: File,
        onProgress: (Int, Int) -> Unit
    ): Result<Unit>
    
    private fun validateZipFile(zipFile: File, expectedSize: Long): Boolean
    private fun cleanupPartialExtraction(destinationDir: File)
}
```

### ImageLoader Integration

The existing image loading mechanism will be enhanced to check for downloaded images.

```kotlin
// Extension function for Coil ImageRequest
fun ImageRequest.Builder.personaImage(
    context: Context,
    game: String,
    name: String
): ImageRequest.Builder {
    val imagePath = File(context.filesDir, "images/personas/$game/${name.lowercase()}.webp")
    
    return if (imagePath.exists()) {
        this.data(imagePath)
    } else {
        this.data(R.drawable.placeholder_persona)
    }
}

fun ImageRequest.Builder.enemyImage(
    context: Context,
    game: String,
    name: String
): ImageRequest.Builder {
    val imagePath = File(context.filesDir, "images/enemies/$game/${name.lowercase()}.webp")
    
    return if (imagePath.exists()) {
        this.data(imagePath)
    } else {
        this.data(R.drawable.placeholder_enemy)
    }
}
```

### Settings UI Integration

The settings screen will display download controls and status.

```kotlin
@Composable
fun ImagesSettingsSection(
    downloadStatus: DownloadStatus,
    downloadProgress: DownloadProgress?,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    // UI implementation showing:
    // - Current status (Downloaded / Not Downloaded)
    // - Storage size if downloaded
    // - Download button with metered warning
    // - Progress indicator during download/extraction
    // - Delete button if downloaded
    // - Resume button if interrupted
}
```

## Data Models

### SharedPreferences Schema

Stored in `image_downloads` preferences file:

```kotlin
data class ImageDownloadPreferences(
    val imagesDownloaded: Boolean,           // "images_downloaded"
    val downloadedVersion: String?,          // "downloaded_version"
    val downloadTimestamp: Long?,            // "download_timestamp"
    val partialDownloadBytes: Long?,         // "partial_download_bytes"
    val partialDownloadTotalBytes: Long?,    // "partial_download_total_bytes"
    val partialDownloadZipPath: String?      // "partial_download_zip_path"
)
```

### File System Structure

```
/data/data/com.persona.companion/
├── files/
│   └── images/                    # Extracted images
│       ├── personas/
│       │   ├── p3/
│       │   │   ├── orpheus.webp
│       │   │   └── thanatos.webp
│       │   ├── p4/
│       │   ├── p5/
│       │   └── ...
│       └── enemies/
│           ├── p3r/
│           ├── p4g/
│           ├── p5r/
│           └── ...
└── cache/
    └── images.zip                 # Temporary download (deleted after extraction)
```

### ZIP File Structure

The ZIP file hosted on the CDN will have this structure:

```
images.zip
├── personas/
│   ├── p3/
│   │   ├── orpheus.webp
│   │   └── ...
│   ├── p4/
│   ├── p5/
│   └── ...
└── enemies/
    ├── p3r/
    ├── p4g/
    ├── p5r/
    └── ...
```

### Build Configuration

CDN URL will be configurable in `build.gradle`:

```groovy
buildTypes {
    debug {
        buildConfigField "String", "IMAGES_CDN_URL", 
            '"https://github.com/username/repo/releases/download/v1.0/images.zip"'
        buildConfigField "long", "IMAGES_ZIP_SIZE", "42000000L"  // ~40MB
        buildConfigField "String", "IMAGES_VERSION", '"1.0"'
    }
    release {
        buildConfigField "String", "IMAGES_CDN_URL", 
            '"https://github.com/username/repo/releases/download/v1.0/images.zip"'
        buildConfigField "long", "IMAGES_ZIP_SIZE", "42000000L"
        buildConfigField "String", "IMAGES_VERSION", '"1.0"'
    }
}
```


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, I identified several areas of redundancy:

- Properties 5.1 and 14.1/14.2 all test placeholder display when images aren't downloaded - these can be combined into one comprehensive property
- Properties 3.5 and 4.1 both test automatic transition to extraction - these are duplicates
- Properties 1.5 and 8.4 both test CDN unreachable error handling - these are duplicates
- Properties 2.6 and 9.1 both test metered connection warning - these are duplicates
- Properties 4.6 and 11.1 both test SharedPreferences persistence - these are duplicates
- Properties 3.2 and 3.3 can be combined into one property about download progress display
- Properties 11.2 and 11.3 can be combined into one property about metadata persistence

After consolidation, the following properties provide unique validation value:

### Property 1: Download from valid CDN URL succeeds

*For any* valid CDN URL pointing to a ZIP file, the Image_Download_Manager should successfully download the file to the cache directory.

**Validates: Requirements 1.1, 3.7**

### Property 2: Download progress reflects actual state

*For any* point during download, the Download_Progress_Indicator should display progress information (bytes downloaded, total bytes, percentage) that accurately reflects the current download state.

**Validates: Requirements 3.2, 3.3**

### Property 3: Download errors include retry option

*For any* download error (network, storage, CDN unreachable), the error message displayed should include a retry button.

**Validates: Requirements 3.6, 8.5**

### Property 4: Extraction preserves directory structure

*For any* ZIP file with nested directories, extracting it should maintain the complete folder structure in the destination directory.

**Validates: Requirements 4.4**

### Property 5: Extraction progress reflects actual state

*For any* point during extraction, the Download_Progress_Indicator should display the number of files extracted versus total files accurately.

**Validates: Requirements 4.3**

### Property 6: Placeholder images shown when not downloaded

*For any* persona or enemy, when images are not downloaded, the Image_Loader should display the appropriate placeholder image.

**Validates: Requirements 5.1, 14.1, 14.2**

### Property 7: Downloaded images load from storage

*For any* persona or enemy, when images are downloaded and the file exists in storage, the Image_Loader should load the image from the Image_Storage directory.

**Validates: Requirements 5.2**

### Property 8: Image paths follow naming convention

*For any* game ID and entity name, the Image_Loader should construct the image path following the format "images/{category}/{game}/{name}.webp".

**Validates: Requirements 5.3**

### Property 9: Missing images fall back to placeholder

*For any* persona or enemy, if the image file is missing from Image_Storage (even when images are marked as downloaded), the Image_Loader should display the placeholder image.

**Validates: Requirements 5.4**

### Property 10: Image caching improves performance

*For any* image, loading it a second time should use the cached version rather than reading from disk again.

**Validates: Requirements 5.5**

### Property 11: Deletion removes all files

*For any* set of files in the Image_Storage directory, confirming deletion should remove all files and subdirectories.

**Validates: Requirements 6.4**

### Property 12: Post-deletion shows placeholders

*For any* persona or enemy, after deleting images, the Image_Loader should display placeholder images.

**Validates: Requirements 6.7**

### Property 13: Download interruption saves progress

*For any* interruption point during download, the Image_Download_Manager should save the current progress (bytes downloaded, total bytes) to SharedPreferences.

**Validates: Requirements 7.1**

### Property 14: Resume continues from saved position

*For any* saved download progress, resuming the download should continue from the saved byte position rather than starting over.

**Validates: Requirements 7.3**

### Property 15: Download metadata persisted

*For any* successful download, the Image_Download_Manager should save both the download timestamp and version number to SharedPreferences.

**Validates: Requirements 11.2, 11.3**

### Property 16: File size verification detects mismatches

*For any* downloaded ZIP file, if the actual file size does not match the expected size from build configuration, the verification should fail.

**Validates: Requirements 15.1**

### Property 17: Platform-appropriate storage location

*For any* Android platform (phone, tablet, TV), the Image_Storage should use the app's internal storage directory (context.filesDir).

**Validates: Requirements 13.5**

### Property 18: UI state reflects download status

*For any* download status (not downloaded, downloading, downloaded), the Settings_Manager should display UI elements appropriate to that state.

**Validates: Requirements 2.2**

### Example-Based Test Cases

The following scenarios should be tested with specific examples rather than property-based testing:

**Example 1: CDN unreachable error**
- Given: CDN URL points to unreachable server
- When: User initiates download
- Then: Error message "Server unavailable. Please try again later." is displayed
- **Validates: Requirements 1.5, 8.4**

**Example 2: Settings screen has Images section**
- Given: User navigates to Settings
- When: Settings screen loads
- Then: An "Images" section is visible
- **Validates: Requirements 2.1**

**Example 3: Not downloaded state shows download button**
- Given: Images are not downloaded
- When: User views Images section in Settings
- Then: "Download Images" button is visible
- **Validates: Requirements 2.3**

**Example 4: Downloaded state shows storage size**
- Given: Images are downloaded
- When: User views Images section in Settings
- Then: Storage size consumed by images is displayed
- **Validates: Requirements 2.4**

**Example 5: Estimated download size displayed**
- Given: Images are not downloaded
- When: User views Images section in Settings
- Then: Estimated download size (~40MB) is displayed
- **Validates: Requirements 2.5**

**Example 6: Metered connection warning**
- Given: Device is on metered connection and images not downloaded
- When: User taps "Download Images"
- Then: Warning dialog about metered connection is displayed
- **Validates: Requirements 2.6, 9.1, 9.2, 9.3**

**Example 7: Download button triggers download**
- Given: Images are not downloaded
- When: User taps "Download Images"
- Then: Download process starts
- **Validates: Requirements 3.1**

**Example 8: Downloading state disables button**
- Given: Download is in progress
- When: User views Images section
- Then: Download button is disabled and shows "Downloading..."
- **Validates: Requirements 3.4**

**Example 9: Successful download triggers extraction**
- Given: ZIP download completes successfully
- When: Download finishes
- Then: Extraction automatically begins
- **Validates: Requirements 3.5, 4.1**

**Example 10: Extracting state shows progress**
- Given: Extraction is in progress
- When: User views Images section
- Then: "Extracting images..." message with progress indicator is displayed
- **Validates: Requirements 4.2**

**Example 11: Successful extraction cleanup**
- Given: Extraction completes successfully
- When: Extraction finishes
- Then: Downloaded ZIP file is deleted from cache
- **Validates: Requirements 4.5**

**Example 12: Successful extraction updates state**
- Given: Extraction completes successfully
- When: Extraction finishes
- Then: SharedPreferences is updated with images_downloaded = true
- **Validates: Requirements 4.6, 11.1**

**Example 13: Extraction failure cleanup**
- Given: Extraction fails (corrupted ZIP)
- When: Extraction error occurs
- Then: Error message is displayed and partial extraction is cleaned up
- **Validates: Requirements 4.7**

**Example 14: Downloaded state shows delete button**
- Given: Images are downloaded
- When: User views Images section
- Then: "Delete Images" button is visible with storage size
- **Validates: Requirements 6.1, 6.2**

**Example 15: Delete prompts confirmation**
- Given: Images are downloaded
- When: User taps "Delete Images"
- Then: Confirmation dialog is displayed
- **Validates: Requirements 6.3**

**Example 16: Deletion updates state**
- Given: User confirms deletion
- When: Deletion completes
- Then: Download status is reset to "Not Downloaded"
- **Validates: Requirements 6.5**

**Example 17: Deletion shows confirmation**
- Given: User confirms deletion
- When: Deletion completes
- Then: Confirmation message showing space freed is displayed
- **Validates: Requirements 6.6**

**Example 18: Interrupted download shows resume**
- Given: Download was interrupted
- When: User returns to Settings
- Then: "Resume Download" button is displayed
- **Validates: Requirements 7.2**

**Example 19: Non-resumable download restarts**
- Given: CDN does not support range requests
- When: User attempts to resume download
- Then: Download restarts from beginning
- **Validates: Requirements 7.5**

**Example 20: Resumed download completes normally**
- Given: Download is resumed
- When: Resumed download completes
- Then: Extraction proceeds automatically
- **Validates: Requirements 7.6**

**Example 21: Network error message**
- Given: Download fails due to network error
- When: Error occurs
- Then: "Network error. Check your connection and try again." is displayed
- **Validates: Requirements 8.1**

**Example 22: Insufficient storage error message**
- Given: Download fails due to insufficient storage
- When: Error occurs
- Then: "Not enough storage space. Free up space and try again." is displayed
- **Validates: Requirements 8.2**

**Example 23: Corrupted ZIP error message**
- Given: Extraction fails due to corrupted ZIP
- When: Error occurs
- Then: "Download corrupted. Please try again." is displayed
- **Validates: Requirements 8.3**

**Example 24: Cancel metered download**
- Given: Metered connection warning is displayed
- When: User selects "Cancel"
- Then: Download does not start
- **Validates: Requirements 9.4**

**Example 25: Confirm metered download**
- Given: Metered connection warning is displayed
- When: User selects "Download"
- Then: Download proceeds
- **Validates: Requirements 9.5**

**Example 26: Storage check before download**
- Given: User initiates download
- When: Download starts
- Then: Available storage space is checked first
- **Validates: Requirements 10.1**

**Example 27: Insufficient storage prevents download**
- Given: Available storage is less than 100 MB
- When: User initiates download
- Then: Error message is displayed and download does not start
- **Validates: Requirements 10.2, 10.3, 10.4**

**Example 28: Storage check before extraction**
- Given: Download completes
- When: Extraction is about to start
- Then: Available storage is verified again
- **Validates: Requirements 10.5**

**Example 29: App startup checks download state**
- Given: App is starting
- When: App initializes
- Then: Image_Download_Manager checks if images were previously downloaded
- **Validates: Requirements 11.4**

**Example 30: Downloaded with existing files shows downloaded**
- Given: SharedPreferences indicates images downloaded and files exist
- When: User views Settings
- Then: "Downloaded" status is displayed
- **Validates: Requirements 11.5**

**Example 31: Downloaded with missing files shows not downloaded**
- Given: SharedPreferences indicates images downloaded but files are missing
- When: User views Settings
- Then: "Not Downloaded" status is displayed
- **Validates: Requirements 11.6**

**Example 32: Progress persists during navigation**
- Given: Download is in progress
- When: User navigates away from Settings
- Then: Download progress indicator remains visible in Settings when returning
- **Validates: Requirements 12.3**

**Example 33: Background download continues**
- Given: Download is in progress
- When: User navigates to another screen
- Then: Download continues in background
- **Validates: Requirements 12.4**

**Example 34: Returning shows current progress**
- Given: Download is in progress and user navigated away
- When: User returns to Settings
- Then: Current download progress is displayed
- **Validates: Requirements 12.5**

**Example 35: ZIP validation before extraction**
- Given: ZIP download completes
- When: Before extraction starts
- Then: ZIP file is validated for corruption
- **Validates: Requirements 15.3**

**Example 36: Corrupted ZIP cleanup**
- Given: ZIP file is corrupted
- When: Validation fails
- Then: ZIP file is deleted and error is displayed
- **Validates: Requirements 15.4**

**Example 37: File size mismatch fails download**
- Given: Downloaded file size does not match expected size
- When: Verification runs
- Then: Download is marked as failed
- **Validates: Requirements 15.2**


## Error Handling

### Error Categories

The system handles four main categories of errors:

1. **Network Errors**: Connection failures, timeouts, CDN unavailability
2. **Storage Errors**: Insufficient space, permission issues, I/O failures
3. **Data Integrity Errors**: Corrupted ZIP, size mismatches, invalid file format
4. **State Errors**: Inconsistent state between SharedPreferences and file system

### Error Handling Strategy

```kotlin
sealed class DownloadError {
    data class NetworkError(val message: String, val cause: Throwable?) : DownloadError()
    data class StorageError(val message: String, val availableSpace: Long) : DownloadError()
    data class IntegrityError(val message: String, val expectedSize: Long, val actualSize: Long) : DownloadError()
    data class StateError(val message: String) : DownloadError()
}

fun DownloadError.toUserMessage(): String = when (this) {
    is NetworkError -> "Network error. Check your connection and try again."
    is StorageError -> "Not enough storage space. Free up space and try again."
    is IntegrityError -> "Download corrupted. Please try again."
    is StateError -> "An error occurred. Please try again."
}
```

### Error Recovery

Each error type has a specific recovery strategy:

**Network Errors:**
- Save current progress to SharedPreferences
- Display error message with retry button
- On retry: attempt to resume using HTTP range requests
- If resume fails: restart download from beginning

**Storage Errors:**
- Check available space before download and before extraction
- Display specific error message with available space information
- Clean up any partial downloads
- Do not save progress (user must free space first)

**Data Integrity Errors:**
- Delete corrupted ZIP file
- Clear any partial extraction
- Reset download state
- Display error message with retry button
- On retry: start fresh download

**State Errors:**
- Detect mismatch between SharedPreferences and file system
- Reconcile state by checking file existence
- Update UI to reflect actual state
- Log inconsistency for debugging

### Cleanup Strategy

The system ensures no orphaned data remains after errors:

```kotlin
suspend fun cleanupAfterError(context: Context, error: DownloadError) {
    when (error) {
        is NetworkError -> {
            // Keep partial download for resume
            // Only save progress if > 10% complete
            if (progress > 0.1) {
                saveDownloadState(context, currentState)
            }
        }
        is StorageError -> {
            // Delete partial download
            File(context.cacheDir, "images.zip").delete()
            clearDownloadState(context)
        }
        is IntegrityError -> {
            // Delete corrupted files
            File(context.cacheDir, "images.zip").delete()
            File(context.filesDir, "images").deleteRecursively()
            clearDownloadState(context)
        }
        is StateError -> {
            // Reconcile state
            reconcileDownloadState(context)
        }
    }
}
```

### Logging

All errors are logged with context for debugging:

```kotlin
private fun logError(error: DownloadError, context: String) {
    when (error) {
        is NetworkError -> Log.e(TAG, "Network error during $context: ${error.message}", error.cause)
        is StorageError -> Log.e(TAG, "Storage error during $context: ${error.message}, available: ${error.availableSpace}")
        is IntegrityError -> Log.e(TAG, "Integrity error during $context: expected ${error.expectedSize}, got ${error.actualSize}")
        is StateError -> Log.e(TAG, "State error during $context: ${error.message}")
    }
}
```

## Testing Strategy

### Dual Testing Approach

The cloud image download system requires both unit tests and property-based tests for comprehensive coverage:

- **Unit tests**: Verify specific examples, edge cases, error conditions, and UI state transitions
- **Property tests**: Verify universal properties across all inputs (download progress, path construction, state persistence)

Unit tests focus on concrete scenarios (e.g., "metered connection shows warning"), while property tests verify general rules (e.g., "for any download progress, UI reflects accurate state"). Together, they provide comprehensive coverage where unit tests catch specific bugs and property tests verify general correctness.

### Property-Based Testing

We will use **Kotest Property Testing** for Kotlin, which provides excellent integration with Android testing and coroutines.

**Configuration:**
- Minimum 100 iterations per property test
- Each test references its design document property
- Tag format: `Feature: cloud-image-download, Property {number}: {property_text}`

**Example property test:**

```kotlin
class ImageLoaderPropertyTest : StringSpec({
    "Feature: cloud-image-download, Property 8: Image paths follow naming convention" {
        checkAll(
            iterations = 100,
            Arb.string(1..20, Codepoint.alphanumeric()),  // game ID
            Arb.string(1..50, Codepoint.alphanumeric()),  // entity name
            Arb.enum<ImageCategory>()  // personas or enemies
        ) { gameId, entityName, category ->
            val path = ImageLoader.constructPath(gameId, entityName, category)
            
            path shouldStartWith "images/"
            path shouldContain "/${category.value}/"
            path shouldContain "/$gameId/"
            path shouldEndWith ".webp"
            path shouldContain entityName.lowercase()
        }
    }
})
```

### Unit Testing

Unit tests will cover specific scenarios and edge cases:

**Download Manager Tests:**
- Storage space verification before download
- Metered connection detection and warning
- Download state persistence
- Resume functionality
- Error handling for each error type
- Cleanup after errors

**Extraction Service Tests:**
- ZIP validation (size, format)
- Directory structure preservation
- Cleanup after successful extraction
- Cleanup after failed extraction
- Progress tracking

**Image Loader Tests:**
- Placeholder display when not downloaded
- Image loading from storage when downloaded
- Fallback to placeholder for missing files
- Path construction for different games
- Memory caching

**Settings UI Tests:**
- UI state for "not downloaded"
- UI state for "downloading"
- UI state for "downloaded"
- Button visibility based on state
- Progress indicator updates
- Error message display

### Integration Testing

Integration tests will verify end-to-end flows:

**Complete Download Flow:**
1. User taps "Download Images"
2. Storage check passes
3. Download progresses with UI updates
4. Download completes
5. Extraction progresses with UI updates
6. Extraction completes
7. State persisted to SharedPreferences
8. UI updates to "Downloaded"
9. Images load from storage

**Download Interruption and Resume:**
1. Start download
2. Simulate interruption (network loss)
3. Progress saved to SharedPreferences
4. User returns to Settings
5. "Resume Download" button visible
6. User taps resume
7. Download continues from saved position
8. Download completes normally

**Delete and Re-download:**
1. Images are downloaded
2. User deletes images
3. Files removed from storage
4. State reset in SharedPreferences
5. UI shows "Not Downloaded"
6. Placeholders displayed for all images
7. User downloads again
8. Images available again

### Test Data

**Mock ZIP Files:**
- Valid ZIP with correct structure
- Corrupted ZIP (invalid format)
- ZIP with incorrect size
- ZIP with missing directories
- Empty ZIP

**Mock CDN Responses:**
- Successful download (200 OK)
- Network error (timeout)
- Server unavailable (503)
- Range request support (206 Partial Content)
- No range request support (200 OK only)

**Test Image Sets:**
- Small set (10 images, ~1MB) for fast tests
- Medium set (100 images, ~10MB) for integration tests
- Full set (1000+ images, ~45MB) for manual testing only

### Performance Testing

While not part of automated testing, these performance characteristics should be manually verified:

- Download speed: Should utilize available bandwidth efficiently
- Extraction speed: Should extract 1000+ files in < 30 seconds
- Memory usage: Should not exceed 100MB during download/extraction
- UI responsiveness: UI should remain responsive during background operations
- Cache efficiency: Second load of same image should be < 1ms

### Test Coverage Goals

- Unit test coverage: > 80% of business logic
- Property test coverage: All 18 identified properties
- Integration test coverage: All critical user flows
- Error scenario coverage: All error types and recovery paths

### Continuous Integration

Tests will run in CI pipeline:
- Unit tests: Run on every commit
- Property tests: Run on every commit (100 iterations)
- Integration tests: Run on every PR
- Manual testing: Required before release

