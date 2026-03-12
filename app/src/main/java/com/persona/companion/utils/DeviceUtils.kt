package com.persona.companion.utils

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.persona.companion.BuildConfig
import com.persona.companion.data.ImageDownloadManager

/**
 * Device type classification
 */
enum class DeviceType {
    PHONE,      // Small screen, touch only
    TABLET,     // Large screen, touch
    TV,         // TV/Android TV, remote/gamepad
    CAST        // Casting to external display
}

/**
 * Screen size classification
 */
enum class ScreenSize {
    COMPACT,    // < 600dp
    MEDIUM,     // 600dp - 840dp
    EXPANDED    // > 840dp
}

/**
 * Device detection and screen size utilities
 */
object DeviceUtils {
    
    /**
     * Detect device type based on configuration
     * Note: Does not check Cast state to avoid initialization issues
     */
    fun getDeviceType(context: Context): DeviceType {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val currentMode = uiModeManager.currentModeType
        
        return when (currentMode) {
            Configuration.UI_MODE_TYPE_TELEVISION -> DeviceType.TV
            else -> {
                val configuration = context.resources.configuration
                val screenWidthDp = configuration.screenWidthDp
                val screenHeightDp = configuration.screenHeightDp
                val smallestWidth = minOf(screenWidthDp, screenHeightDp)
                
                when {
                    smallestWidth >= 600 -> DeviceType.TABLET
                    else -> DeviceType.PHONE
                }
            }
        }
    }
    
    /**
     * Get screen size classification
     */
    fun getScreenSize(widthDp: Int): ScreenSize {
        return when {
            widthDp < 600 -> ScreenSize.COMPACT
            widthDp < 840 -> ScreenSize.MEDIUM
            else -> ScreenSize.EXPANDED
        }
    }
    
    /**
     * Check if device is TV
     */
    fun isTV(context: Context): Boolean {
        return getDeviceType(context) == DeviceType.TV
    }
    
    /**
     * Check if device is tablet
     */
    fun isTablet(context: Context): Boolean {
        return getDeviceType(context) == DeviceType.TABLET
    }
    
    /**
     * Check if device is phone
     */
    fun isPhone(context: Context): Boolean {
        return getDeviceType(context) == DeviceType.PHONE
    }
    
    /**
     * Check if images should be loaded for this device
     * Now checks if images are actually available (bundled or downloaded)
     */
    fun shouldLoadImages(context: Context): Boolean {
        // Check if images are bundled in the APK
        if (BuildConfig.INCLUDE_IMAGES) {
            return true
        }
        
        // Check if images have been downloaded (use ImageDownloadManager)
        return ImageDownloadManager.areImagesDownloaded(context)
    }
    
    /**
     * Get appropriate image resolution for device
     */
    fun getImageResolution(context: Context): ImageResolution {
        return when (getDeviceType(context)) {
            DeviceType.PHONE -> ImageResolution.NONE
            DeviceType.TABLET -> ImageResolution.MEDIUM
            DeviceType.TV, DeviceType.CAST -> ImageResolution.HIGH
        }
    }
}

/**
 * Image resolution levels
 */
enum class ImageResolution {
    NONE,       // No images
    MEDIUM,     // Medium resolution for tablets
    HIGH        // High resolution for TV
}

/**
 * Composable to remember device type
 */
@Composable
fun rememberDeviceType(): DeviceType {
    val context = LocalContext.current
    return remember { DeviceUtils.getDeviceType(context) }
}

/**
 * Composable to remember screen size
 */
@Composable
fun rememberScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        DeviceUtils.getScreenSize(configuration.screenWidthDp)
    }
}

/**
 * Composable to check if images should be loaded
 */
@Composable
fun rememberShouldLoadImages(): Boolean {
    val context = LocalContext.current
    return remember { DeviceUtils.shouldLoadImages(context) }
}

/**
 * Get content padding based on device type
 */
@Composable
fun rememberContentPadding(): Dp {
    val deviceType = rememberDeviceType()
    return when (deviceType) {
        DeviceType.PHONE -> 16.dp
        DeviceType.TABLET -> 24.dp
        DeviceType.TV -> 48.dp
        DeviceType.CAST -> 32.dp
    }
}

/**
 * Get text scale factor for TV readability
 */
@Composable
fun rememberTextScaleFactor(): Float {
    val deviceType = rememberDeviceType()
    return when (deviceType) {
        DeviceType.TV -> 1.3f  // Larger text for TV
        else -> 1.0f
    }
}
