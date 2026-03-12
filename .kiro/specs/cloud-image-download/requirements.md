# Requirements Document

## Introduction

The Persona Companion app currently bundles all persona and enemy images within the APK, resulting in a ~50MB package size. This cloud-based image download system will reduce the APK size to ~5MB by hosting all images as a single ZIP file on a CDN. Users can download and extract the ZIP file from the app settings to enable image viewing. This approach is simpler than individual image downloads and provides full offline access once the ZIP is downloaded and extracted.

## Glossary

- **Image_Download_Manager**: The component responsible for downloading and extracting the images ZIP file from the CDN
- **CDN**: Content Delivery Network - the remote storage service hosting the images ZIP file (GitHub Releases, Cloudflare R2, or similar)
- **Images_ZIP**: A single compressed archive containing all persona and enemy images organized by category
- **Image_Storage**: Local storage directory in the app's file system where extracted images are stored
- **Placeholder_Image**: A default image displayed when images have not been downloaded yet
- **Settings_Manager**: The component that manages the image download feature in app settings
- **Image_Loader**: The component that loads images for display from local storage
- **Download_Progress_Indicator**: UI element showing the status of ZIP download and extraction
- **Storage_Size**: The total disk space consumed by extracted images (~45MB)

## Requirements

### Requirement 1: ZIP File Hosting

**User Story:** As a developer, I want to host all images as a single ZIP file on a free CDN, so that the APK size is reduced from ~50MB to ~5MB

#### Acceptance Criteria

1. THE Image_Download_Manager SHALL retrieve the Images_ZIP from a configurable CDN URL
2. THE Images_ZIP SHALL contain all persona and enemy images organized in subdirectories (personas/p3, personas/p4, enemies/p3r, etc.)
3. THE Images_ZIP SHALL be compressed to minimize download size (target ~40MB compressed)
4. THE CDN URL SHALL be configurable in the app's build configuration
5. WHEN the CDN is unreachable, THE Image_Download_Manager SHALL return an error indicating network connectivity issues

### Requirement 2: Settings Integration

**User Story:** As a user, I want to download images from the app settings, so that I can enable image viewing when I need it

#### Acceptance Criteria

1. THE Settings_Manager SHALL provide an "Images" section in the settings screen
2. THE Settings_Manager SHALL display the current download status ("Not Downloaded" or "Downloaded")
3. WHEN images are not downloaded, THE Settings_Manager SHALL display a "Download Images" button
4. WHEN images are downloaded, THE Settings_Manager SHALL display the Storage_Size consumed by images
5. THE Settings_Manager SHALL display an estimated download size before downloading (~40MB)
6. THE Settings_Manager SHALL warn users on metered connections before starting the download

### Requirement 3: ZIP Download

**User Story:** As a user, I want to download the images ZIP file, so that I can view persona and enemy images in the app

#### Acceptance Criteria

1. WHEN the user taps "Download Images", THE Image_Download_Manager SHALL start downloading the Images_ZIP from the CDN
2. WHILE downloading, THE Download_Progress_Indicator SHALL display a progress bar showing percentage complete
3. WHILE downloading, THE Download_Progress_Indicator SHALL display the downloaded size versus total size (e.g., "15 MB / 40 MB")
4. WHILE downloading, THE Settings_Manager SHALL disable the download button and show "Downloading..."
5. WHEN the download completes successfully, THE Image_Download_Manager SHALL proceed to extraction automatically
6. WHEN the download fails, THE Download_Progress_Indicator SHALL display an error message with retry option
7. THE Image_Download_Manager SHALL download the ZIP to the app's cache directory first

### Requirement 4: ZIP Extraction

**User Story:** As a user, I want the downloaded ZIP to be extracted automatically, so that images are ready to use without manual intervention

#### Acceptance Criteria

1. WHEN the Images_ZIP download completes, THE Image_Download_Manager SHALL automatically begin extraction
2. WHILE extracting, THE Download_Progress_Indicator SHALL display "Extracting images..." with a progress indicator
3. WHILE extracting, THE Download_Progress_Indicator SHALL display the number of files extracted versus total files
4. THE Image_Download_Manager SHALL extract images to the Image_Storage directory maintaining the folder structure
5. WHEN extraction completes successfully, THE Image_Download_Manager SHALL delete the downloaded ZIP file to free space
6. WHEN extraction completes successfully, THE Image_Download_Manager SHALL mark images as downloaded in SharedPreferences
7. WHEN extraction fails, THE Image_Download_Manager SHALL display an error message and clean up partial extraction

### Requirement 5: Image Loading

**User Story:** As a user, I want to see persona and enemy images after downloading, so that I can visually identify characters and enemies

#### Acceptance Criteria

1. WHEN images are not downloaded, THE Image_Loader SHALL display the Placeholder_Image for all personas and enemies
2. WHEN images are downloaded, THE Image_Loader SHALL load images from the Image_Storage directory
3. THE Image_Loader SHALL construct image paths based on game ID and entity name (e.g., "personas/p5/arsene.webp")
4. IF an image file is missing from Image_Storage, THEN THE Image_Loader SHALL display the Placeholder_Image
5. THE Image_Loader SHALL cache loaded images in memory to improve performance
6. THE Image_Loader SHALL work identically on phone, tablet, and Android TV platforms

### Requirement 6: Storage Management

**User Story:** As a user, I want to delete downloaded images to free up storage space, so that I can manage my device's storage

#### Acceptance Criteria

1. WHEN images are downloaded, THE Settings_Manager SHALL display a "Delete Images" button
2. THE Settings_Manager SHALL display the Storage_Size next to the delete button
3. WHEN the user taps "Delete Images", THE Settings_Manager SHALL prompt for confirmation
4. WHEN the user confirms deletion, THE Image_Download_Manager SHALL delete all files in the Image_Storage directory
5. WHEN deletion completes, THE Image_Download_Manager SHALL reset the download status to "Not Downloaded"
6. WHEN deletion completes, THE Settings_Manager SHALL display a confirmation message showing the amount of space freed
7. AFTER deletion, THE Image_Loader SHALL display Placeholder_Image for all personas and enemies

### Requirement 7: Download Progress Persistence

**User Story:** As a user, I want my download to resume if interrupted, so that I don't have to start over from the beginning

#### Acceptance Criteria

1. WHEN a download is interrupted (app closed, network lost), THE Image_Download_Manager SHALL save the download progress
2. WHEN the user returns to settings after interruption, THE Settings_Manager SHALL display "Resume Download" button
3. WHEN the user taps "Resume Download", THE Image_Download_Manager SHALL resume from the last saved position
4. THE Image_Download_Manager SHALL use HTTP range requests to resume partial downloads
5. IF the CDN does not support range requests, THEN THE Image_Download_Manager SHALL restart the download from the beginning
6. WHEN a resumed download completes, THE Image_Download_Manager SHALL proceed to extraction normally

### Requirement 8: Error Handling

**User Story:** As a user, I want clear error messages when downloads fail, so that I understand what went wrong and how to fix it

#### Acceptance Criteria

1. WHEN download fails due to network error, THE Download_Progress_Indicator SHALL display "Network error. Check your connection and try again."
2. WHEN download fails due to insufficient storage, THE Download_Progress_Indicator SHALL display "Not enough storage space. Free up space and try again."
3. WHEN extraction fails due to corrupted ZIP, THE Download_Progress_Indicator SHALL display "Download corrupted. Please try again."
4. WHEN the CDN is unreachable, THE Download_Progress_Indicator SHALL display "Server unavailable. Please try again later."
5. ALL error messages SHALL include a "Retry" button to attempt the operation again
6. THE Image_Download_Manager SHALL log all errors for debugging purposes

### Requirement 9: Network Efficiency

**User Story:** As a user on a metered connection, I want to be warned before downloading large files, so that I can avoid unexpected data charges

#### Acceptance Criteria

1. WHEN the user taps "Download Images" on a metered connection, THE Settings_Manager SHALL display a warning dialog
2. THE warning dialog SHALL state "You are on a metered connection. This will download approximately 40 MB. Continue?"
3. THE warning dialog SHALL provide "Download" and "Cancel" options
4. WHEN the user selects "Cancel", THE Image_Download_Manager SHALL not start the download
5. WHEN the user selects "Download", THE Image_Download_Manager SHALL proceed with the download
6. THE Image_Download_Manager SHALL use HTTP connection pooling and appropriate timeouts (60 seconds)

### Requirement 10: Storage Space Verification

**User Story:** As a user, I want the app to check available storage before downloading, so that I don't run out of space mid-download

#### Acceptance Criteria

1. BEFORE starting download, THE Image_Download_Manager SHALL check available storage space on the device
2. IF available storage is less than 100 MB (ZIP + extracted images), THEN THE Image_Download_Manager SHALL display an error
3. THE error message SHALL state "Not enough storage space. You need at least 100 MB free. Currently available: X MB"
4. THE Image_Download_Manager SHALL not start the download if storage check fails
5. AFTER download completes but before extraction, THE Image_Download_Manager SHALL verify storage again for extraction

### Requirement 11: Download State Persistence

**User Story:** As a user, I want the app to remember that I've downloaded images, so that I don't have to download them again after app updates

#### Acceptance Criteria

1. WHEN images are successfully downloaded and extracted, THE Image_Download_Manager SHALL save the download status in SharedPreferences
2. THE Image_Download_Manager SHALL save the download timestamp to track when images were downloaded
3. THE Image_Download_Manager SHALL save the ZIP version number to track which version was downloaded
4. WHEN the app starts, THE Image_Download_Manager SHALL check if images were previously downloaded
5. IF images were previously downloaded and files still exist, THEN THE Settings_Manager SHALL display "Downloaded" status
6. IF images were previously downloaded but files are missing, THEN THE Settings_Manager SHALL display "Not Downloaded" status

### Requirement 12: Background Download Support

**User Story:** As a user, I want to continue using the app while images download, so that I'm not blocked from accessing other features

#### Acceptance Criteria

1. THE Image_Download_Manager SHALL perform downloads on a background thread using Kotlin coroutines
2. WHILE downloading, THE user SHALL be able to navigate to other screens in the app
3. WHILE downloading, THE Download_Progress_Indicator SHALL remain visible in the settings screen
4. IF the user navigates away from settings during download, THE download SHALL continue in the background
5. WHEN the user returns to settings during download, THE Download_Progress_Indicator SHALL show current progress
6. THE Image_Download_Manager SHALL not block the UI thread during download or extraction

### Requirement 13: Multi-Platform Support

**User Story:** As a user on phone, tablet, or Android TV, I want the image download system to work consistently, so that I have the same experience across devices

#### Acceptance Criteria

1. THE Image_Download_Manager SHALL function identically on phone, tablet, and Android TV platforms
2. THE Settings_Manager SHALL provide touch-friendly UI controls on phone and tablet devices
3. THE Settings_Manager SHALL provide D-pad navigation support for Android TV devices
4. THE Download_Progress_Indicator SHALL scale appropriately for different screen sizes and orientations
5. THE Image_Storage SHALL use platform-appropriate storage locations (app's internal storage)

### Requirement 14: Placeholder Image Display

**User Story:** As a user who hasn't downloaded images, I want to see placeholder images, so that I know where images will appear once downloaded

#### Acceptance Criteria

1. WHEN images are not downloaded, THE Image_Loader SHALL display a generic placeholder for all personas
2. WHEN images are not downloaded, THE Image_Loader SHALL display a generic placeholder for all enemies
3. THE Placeholder_Image SHALL be a simple icon or silhouette indicating missing image
4. THE Placeholder_Image SHALL be bundled in the APK and not require download
5. THE Placeholder_Image SHALL be small in file size (< 5 KB) to minimize APK size impact

### Requirement 15: ZIP File Integrity Verification

**User Story:** As a user, I want the app to verify the downloaded ZIP is not corrupted, so that extraction doesn't fail with partial data

#### Acceptance Criteria

1. WHEN the ZIP download completes, THE Image_Download_Manager SHALL verify the file size matches the expected size
2. IF the downloaded file size does not match expected size, THEN THE Image_Download_Manager SHALL mark download as failed
3. BEFORE extraction, THE Image_Download_Manager SHALL verify the ZIP file is valid and not corrupted
4. IF the ZIP file is corrupted, THEN THE Image_Download_Manager SHALL delete it and display an error
5. THE Image_Download_Manager SHALL log all verification failures for debugging purposes
