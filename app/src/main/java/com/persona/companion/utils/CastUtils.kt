package com.persona.companion.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState

/**
 * Cast utilities for Chromecast/Google Cast support
 */
object CastUtils {
    
    /**
     * Check if device is currently casting
     */
    fun isCasting(context: Context): Boolean {
        return try {
            val castContext = CastContext.getSharedInstance(context)
            castContext.castState == CastState.CONNECTED
        } catch (e: Exception) {
            // Cast not available or not initialized
            false
        }
    }
    
    /**
     * Check if cast is available on this device
     */
    fun isCastAvailable(context: Context): Boolean {
        return try {
            CastContext.getSharedInstance(context)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get current cast state
     */
    fun getCastState(context: Context): Int {
        return try {
            val castContext = CastContext.getSharedInstance(context)
            castContext.castState
        } catch (e: Exception) {
            CastState.NO_DEVICES_AVAILABLE
        }
    }
}

/**
 * Composable to observe cast state
 */
@Composable
fun rememberIsCasting(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = false) {
        value = CastUtils.isCasting(context)
        // In a real implementation, you'd add a listener here
        // to update when cast state changes
    }
}

/**
 * Composable to check if cast is available
 */
@Composable
fun rememberCastAvailable(): Boolean {
    val context = LocalContext.current
    return CastUtils.isCastAvailable(context)
}
