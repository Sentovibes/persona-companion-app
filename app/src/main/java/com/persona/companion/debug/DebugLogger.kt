package com.persona.companion.debug

import android.util.Log
import com.persona.companion.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private val logs = mutableListOf<LogEntry>()
    private const val MAX_LOGS = 1000
    
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )
    
    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    fun d(tag: String, message: String) {
        if (BuildConfig.ENABLE_DEBUG_FEATURES) {
            Log.d(tag, message)
            addLog(LogLevel.DEBUG, tag, message)
        }
    }
    
    fun i(tag: String, message: String) {
        if (BuildConfig.ENABLE_DEBUG_FEATURES) {
            Log.i(tag, message)
            addLog(LogLevel.INFO, tag, message)
        }
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.ENABLE_DEBUG_FEATURES) {
            Log.w(tag, message, throwable)
            addLog(LogLevel.WARNING, tag, message, throwable)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.ENABLE_DEBUG_FEATURES) {
            Log.e(tag, message, throwable)
            addLog(LogLevel.ERROR, tag, message, throwable)
        }
    }
    
    private fun addLog(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        synchronized(logs) {
            logs.add(LogEntry(System.currentTimeMillis(), level, tag, message, throwable))
            if (logs.size > MAX_LOGS) {
                logs.removeAt(0)
            }
        }
    }
    
    fun getLogs(): List<LogEntry> = synchronized(logs) { logs.toList() }
    
    fun clearLogs() = synchronized(logs) { logs.clear() }
    
    fun getFormattedLogs(): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return getLogs().joinToString("\n") { entry ->
            val time = dateFormat.format(Date(entry.timestamp))
            val level = entry.level.name[0]
            val throwableInfo = entry.throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
            "[$time] $level/${entry.tag}: ${entry.message}$throwableInfo"
        }
    }
}
