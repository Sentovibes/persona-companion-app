package com.persona.companion.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

data class AppSettings(
    val showDlc: Boolean = true,
    val showEpisodeAigis: Boolean = true
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    fun getSettings(): AppSettings {
        return AppSettings(
            showDlc = prefs.getBoolean("show_dlc", true),
            showEpisodeAigis = prefs.getBoolean("show_episode_aigis", true)
        )
    }

    fun getSettingsFlow(): Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "show_dlc" || key == "show_episode_aigis") {
                trySend(getSettings())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSettings())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    
    fun setShowDlc(show: Boolean) {
        prefs.edit().putBoolean("show_dlc", show).apply()
    }
    
    fun setShowEpisodeAigis(show: Boolean) {
        prefs.edit().putBoolean("show_episode_aigis", show).apply()
    }

    fun getLastUpdateCheck(): Long {
        return prefs.getLong("last_update_check", 0)
    }
    
    fun setLastUpdateCheck(timestamp: Long) {
        prefs.edit().putLong("last_update_check", timestamp).apply()
    }
    
    fun shouldCheckForUpdates(): Boolean {
        val lastCheck = getLastUpdateCheck()
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000
        return (now - lastCheck) >= dayInMillis
    }
}
