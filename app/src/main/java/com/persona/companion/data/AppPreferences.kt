package com.persona.companion.data

import android.content.Context
import android.content.SharedPreferences

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
