# Implementation Plan: Cloud Image Download System

## Overview

This implementation plan transforms the Persona Companion app from bundling all images in the APK (~50MB) to downloading them on-demand from a CDN as a single ZIP file, reducing the base APK size to ~5MB. The implementation follows a layered approach: core services first, then UI integration, and finally testing and polish.

## Tasks

- [ ] 1. Set up build configuration and data models
  - Add CDN URL, ZIP size, and version to build.gradle as buildConfigField values
  - Create DownloadStatus, DownloadProgress, DownloadPhase, and DownloadState data classes
  - Create DownloadError sealed class with NetworkError, StorageError, IntegrityError, and StateError subtypes
  - Create SharedPreferences helper for image download state persistence
  - _Requirements: 1.4, 11.1, 11.2, 11.3_

- [ ]* 1.1 Write property test for SharedPreferences persistence
  - **Property 15: Download metadata persisted**
  - **Validates: Requirements 11.2, 11.3**

- [ ] 2. Implement DownloadService for HTTP downloads
  - [ ] 2.1 Create DownloadService class with downloadZip method
    - Implement HTTP download using HttpURLConnection with progress callbacks
    - Support HTTP range requests for resume functionality
    - Include connection timeout configuration (60 seconds)
    - _Requirements: 3.1, 3.7, 7.4, 9.6_

  - [ ]* 2.2 Write property test for download progress tracking
    - **Property 2: Download progress reflects actual state**
    - **Validates: Requirements 3.2, 3.3**

  - [ ] 2.3 Add range request support detection
    - Implement supportsRangeRequests method to check Accept-Ranges header
    - Handle resume from byte position when supported
    - _Requirements: 7.4, 7.5_

  - [ ]* 2.4 Write property test for resume functionality
    - **Property 14: Resume continues from saved position**
    - **Validates: Requirements 7.3**

- [ ] 3. Implement ExtractionService for ZIP handling
  - [ ] 3.1 Create ExtractionService class with extractZip method
    - Implement ZIP extraction maintaining directory structure
    - Include progress callbacks for extraction tracking
    - _Requirements: 4.1, 4.4_

  - [ ]* 3.2 Write property test for directory structure preservation
    - **Property 4: Extraction preserves directory structure**
    - **Validates: Requirements 4.4**

  - [ ]* 3.3 Write property test for extraction progress tracking
    - **Property 5: Extraction progress reflects actual state**
    - **Validates: Requirements 4.3**

  - [ ] 3.4 Add ZIP validation logic
    - Implement validateZipFile method to check file size and format
    - Add cleanup logic for partial or corrupted extractions
    - _Requirements: 15.1, 15.3, 15.4_

  - [ ]* 3.5 Write property test for file size verification
    - **Property 16: File size verification detects mismatches**
    - **Validates: Requirements 15.1**

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement ImageDownloadManager orchestration
  - [ ] 5.1 Create ImageDownloadManager object with state query methods
    - Implement areImagesDownloaded, getDownloadStatus, and getStorageSize methods
    - Add storage space verification (checkStorageSpace method)
    - Add metered connection detection (isMeteredConnection method)
    - _Requirements: 2.2, 10.1, 10.2, 2.6_

  - [ ]* 5.2 Write property test for storage location
    - **Property 17: Platform-appropriate storage location**
    - **Validates: Requirements 13.5**

  - [ ] 5.3 Implement startDownload method
    - Orchestrate storage check, download, extraction, and state persistence
    - Emit progress updates through onProgress callback
    - Handle errors and cleanup appropriately
    - _Requirements: 3.1, 3.5, 4.1, 4.5, 4.6_

  - [ ]* 5.4 Write property test for download interruption handling
    - **Property 13: Download interruption saves progress**
    - **Validates: Requirements 7.1**

  - [ ] 5.5 Implement resumeDownload method
    - Load saved download state from SharedPreferences
    - Resume download from saved byte position
    - Fall back to fresh download if resume not supported
    - _Requirements: 7.2, 7.3, 7.5_

  - [ ] 5.6 Implement deleteImages method
    - Recursively delete images directory
    - Clear SharedPreferences state
    - Return amount of space freed
    - _Requirements: 6.4, 6.5_

  - [ ]* 5.7 Write property test for deletion cleanup
    - **Property 11: Deletion removes all files**
    - **Validates: Requirements 6.4**

  - [ ] 5.8 Add error handling and cleanup logic
    - Implement cleanupAfterError method for each error type
    - Add logging for all error scenarios
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [ ]* 5.9 Write property test for error retry options
    - **Property 3: Download errors include retry option**
    - **Validates: Requirements 3.6, 8.5**

- [ ] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Implement ImageLoader integration
  - [ ] 7.1 Create extension functions for Coil ImageRequest
    - Implement personaImage extension to construct persona image paths
    - Implement enemyImage extension to construct enemy image paths
    - Add fallback to placeholder images when files don't exist
    - _Requirements: 5.2, 5.3, 5.4_

  - [ ]* 7.2 Write property test for image path construction
    - **Property 8: Image paths follow naming convention**
    - **Validates: Requirements 5.3**

  - [ ]* 7.3 Write property test for placeholder fallback
    - **Property 6: Placeholder images shown when not downloaded**
    - **Validates: Requirements 5.1, 14.1, 14.2**

  - [ ]* 7.4 Write property test for downloaded image loading
    - **Property 7: Downloaded images load from storage**
    - **Validates: Requirements 5.2**

  - [ ]* 7.5 Write property test for missing image fallback
    - **Property 9: Missing images fall back to placeholder**
    - **Validates: Requirements 5.4**

  - [ ] 7.2 Add placeholder image resources
    - Create placeholder_persona.xml drawable
    - Create placeholder_enemy.xml drawable
    - Keep file sizes minimal (< 5 KB each)
    - _Requirements: 14.3, 14.4_

  - [ ] 7.3 Configure Coil memory caching
    - Ensure Coil ImageLoader has memory cache enabled
    - Verify cache configuration for performance
    - _Requirements: 5.5_

  - [ ]* 7.6 Write property test for image caching
    - **Property 10: Image caching improves performance**
    - **Validates: Requirements 5.5**

- [ ] 8. Implement Settings UI integration
  - [ ] 8.1 Create ImagesSettingsSection composable
    - Display current download status (Downloaded / Not Downloaded)
    - Show storage size when downloaded
    - Show estimated download size when not downloaded
    - _Requirements: 2.1, 2.2, 2.4, 2.5_

  - [ ]* 8.2 Write property test for UI state reflection
    - **Property 18: UI state reflects download status**
    - **Validates: Requirements 2.2**

  - [ ] 8.2 Add download button with metered connection warning
    - Show "Download Images" button when not downloaded
    - Display warning dialog on metered connections
    - Disable button during download showing "Downloading..."
    - _Requirements: 2.3, 2.6, 3.4, 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ] 8.3 Add progress indicator for download and extraction
    - Display progress bar with percentage during download
    - Show downloaded size vs total size (e.g., "15 MB / 40 MB")
    - Display "Extracting images..." with file count during extraction
    - _Requirements: 3.2, 3.3, 4.2, 4.3_

  - [ ] 8.4 Add delete button and confirmation dialog
    - Show "Delete Images" button when downloaded
    - Display storage size next to delete button
    - Show confirmation dialog before deletion
    - Display confirmation message with space freed after deletion
    - _Requirements: 6.1, 6.2, 6.3, 6.6_

  - [ ] 8.5 Add resume button for interrupted downloads
    - Show "Resume Download" button when download is interrupted
    - Resume download from saved progress when tapped
    - _Requirements: 7.2_

  - [ ] 8.6 Add error message display with retry button
    - Display appropriate error messages for each error type
    - Include "Retry" button for all error scenarios
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [ ] 8.7 Integrate ImagesSettingsSection into SettingsScreen
    - Add Images section to existing SettingsScreen
    - Wire up ViewModel to manage download state
    - Ensure background download continues during navigation
    - _Requirements: 2.1, 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 9. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Update existing screens to use new ImageLoader
  - [ ] 10.1 Update persona detail screens
    - Replace bundled image loading with personaImage extension
    - Verify placeholder display when images not downloaded
    - Test image loading after download
    - _Requirements: 5.1, 5.2, 5.4_

  - [ ] 10.2 Update enemy detail screens
    - Replace bundled image loading with enemyImage extension
    - Verify placeholder display when images not downloaded
    - Test image loading after download
    - _Requirements: 5.1, 5.2, 5.4_

  - [ ]* 10.3 Write property test for post-deletion placeholders
    - **Property 12: Post-deletion shows placeholders**
    - **Validates: Requirements 6.7**

  - [ ] 10.3 Verify multi-platform support
    - Test on phone emulator
    - Test on tablet emulator
    - Test on Android TV emulator
    - Verify D-pad navigation on TV
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 11. Add app startup initialization
  - [ ] 11.1 Implement download state reconciliation
    - Check SharedPreferences for download status on app startup
    - Verify image files exist if marked as downloaded
    - Update state to "Not Downloaded" if files are missing
    - _Requirements: 11.4, 11.5, 11.6_

  - [ ] 11.2 Add initialization to Application class
    - Call ImageDownloadManager initialization on app startup
    - Ensure initialization doesn't block main thread
    - _Requirements: 11.4_

- [ ] 12. Write unit tests for specific scenarios
  - [ ]* 12.1 Write unit tests for error scenarios
    - Test CDN unreachable error message
    - Test network error message
    - Test insufficient storage error message
    - Test corrupted ZIP error message
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [ ]* 12.2 Write unit tests for UI state transitions
    - Test Settings screen has Images section
    - Test not downloaded state shows download button
    - Test downloaded state shows storage size and delete button
    - Test downloading state disables button
    - Test interrupted download shows resume button
    - _Requirements: 2.1, 2.3, 2.4, 3.4, 7.2_

  - [ ]* 12.3 Write unit tests for download flow
    - Test download button triggers download
    - Test successful download triggers extraction
    - Test successful extraction updates SharedPreferences
    - Test successful extraction deletes ZIP file
    - _Requirements: 3.1, 3.5, 4.1, 4.5, 4.6_

  - [ ]* 12.4 Write unit tests for deletion flow
    - Test delete prompts confirmation
    - Test deletion updates state to not downloaded
    - Test deletion shows confirmation message
    - _Requirements: 6.3, 6.5, 6.6_

  - [ ]* 12.5 Write unit tests for metered connection handling
    - Test metered connection shows warning
    - Test cancel on metered warning prevents download
    - Test confirm on metered warning starts download
    - _Requirements: 2.6, 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ]* 12.6 Write unit tests for storage verification
    - Test storage check before download
    - Test insufficient storage prevents download
    - Test storage check before extraction
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [ ]* 12.7 Write unit tests for resume functionality
    - Test non-resumable download restarts
    - Test resumed download completes normally
    - _Requirements: 7.5, 7.6_

  - [ ]* 12.8 Write unit tests for state persistence
    - Test app startup checks download state
    - Test downloaded with existing files shows downloaded
    - Test downloaded with missing files shows not downloaded
    - _Requirements: 11.4, 11.5, 11.6_

  - [ ]* 12.9 Write unit tests for ZIP validation
    - Test ZIP validation before extraction
    - Test corrupted ZIP cleanup
    - Test file size mismatch fails download
    - _Requirements: 15.2, 15.3, 15.4_

- [ ] 13. Write integration tests for end-to-end flows
  - [ ]* 13.1 Write integration test for complete download flow
    - Test full flow from download button to images available
    - Verify storage check, download, extraction, state persistence, and UI updates
    - _Requirements: 3.1, 3.5, 4.1, 4.5, 4.6, 11.1_

  - [ ]* 13.2 Write integration test for download interruption and resume
    - Test download interruption, progress save, resume, and completion
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ]* 13.3 Write integration test for delete and re-download
    - Test deletion, state reset, placeholder display, and re-download
    - _Requirements: 6.4, 6.5, 6.7_

  - [ ]* 13.4 Write integration test for background download
    - Test download continues during navigation
    - Test progress persists when returning to settings
    - _Requirements: 12.3, 12.4, 12.5_

- [ ] 14. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties using Kotest
- Unit tests validate specific examples, edge cases, and error conditions
- Integration tests validate end-to-end user flows
- The implementation uses Kotlin with Android Jetpack Compose for UI
- Background operations use Kotlin coroutines for non-blocking execution
- Image loading uses Coil library with memory caching
- All downloads occur on background threads to maintain UI responsiveness
