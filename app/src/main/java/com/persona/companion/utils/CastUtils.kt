package com.persona.companion.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
    
    /**
     * Initialize Cast SDK lazily
     * Call this before using Cast features
     */
    fun initializeCast(context: Context) {
        try {
            CastContext.getSharedInstance(context)
        } catch (e: Exception) {
            // Cast not available, ignore
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
 * Returns false if Cast SDK fails to initialize
 */
@Composable
fun rememberCastAvailable(): Boolean {
    val context = LocalContext.current
    return remember {
        try {
            CastUtils.isCastAvailable(context)
        } catch (e: Exception) {
            false
        }
    }
}
