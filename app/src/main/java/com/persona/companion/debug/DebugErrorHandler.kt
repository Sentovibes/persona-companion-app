package com.persona.companion.debug

import android.app.Application
import com.persona.companion.BuildConfig

class DebugErrorHandler private constructor() {
    
    companion object {
        private var defaultHandler: Thread.UncaughtExceptionHandler? = null
        private val errorCallbacks = mutableListOf<(Throwable) -> Unit>()
        
        fun install(application: Application) {
            if (!BuildConfig.ENABLE_DEBUG_FEATURES) return
            
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                DebugLogger.e("UncaughtException", "Crash in thread: ${thread.name}", throwable)
                
                // Notify callbacks
                errorCallbacks.forEach { callback ->
                    try {
                        callback(throwable)
                    } catch (e: Exception) {
                        DebugLogger.e("DebugErrorHandler", "Error in callback", e)
                    }
                }
                
                // Don't crash in debug mode - just log it
                // In release mode, let it crash normally
                if (!BuildConfig.ENABLE_DEBUG_FEATURES) {
                    defaultHandler?.uncaughtException(thread, throwable)
                }
            }
        }
        
        fun addErrorCallback(callback: (Throwable) -> Unit) {
            errorCallbacks.add(callback)
        }
        
        fun removeErrorCallback(callback: (Throwable) -> Unit) {
            errorCallbacks.remove(callback)
        }
    }
}
