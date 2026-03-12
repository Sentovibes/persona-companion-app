package com.persona.companion.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Dark mode
    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean("dark_mode", enabled).apply()
    
    // Compact view
    fun isCompactView(): Boolean = prefs.getBoolean("compact_view", false)
    fun setCompactView(enabled: Boolean) = prefs.edit().putBoolean("compact_view", enabled).apply()
    
    // Favorites - Personas
    fun getFavoritePersonas(): Set<String> = prefs.getStringSet("favorite_personas", emptySet()) ?: emptySet()
    fun addFavoritePersona(personaId: String) {
        val favorites = getFavoritePersonas().toMutableSet()
        favorites.add(personaId)
        prefs.edit().putStringSet("favorite_personas", favorites).apply()
    }
    fun removeFavoritePersona(personaId: String) {
        val favorites = getFavoritePersonas().toMutableSet()
        favorites.remove(personaId)
        prefs.edit().putStringSet("favorite_personas", favorites).apply()
    }
    fun isFavoritePersona(personaId: String): Boolean = getFavoritePersonas().contains(personaId)
    
    // Favorites - Enemies
    fun getFavoriteEnemies(): Set<String> = prefs.getStringSet("favorite_enemies", emptySet()) ?: emptySet()
    fun addFavoriteEnemy(enemyId: String) {
        val favorites = getFavoriteEnemies().toMutableSet()
        favorites.add(enemyId)
        prefs.edit().putStringSet("favorite_enemies", favorites).apply()
    }
    fun removeFavoriteEnemy(enemyId: String) {
        val favorites = getFavoriteEnemies().toMutableSet()
        favorites.remove(enemyId)
        prefs.edit().putStringSet("favorite_enemies", favorites).apply()
    }
    fun isFavoriteEnemy(enemyId: String): Boolean = getFavoriteEnemies().contains(enemyId)
    
    // Recently viewed - Personas
    fun getRecentPersonas(): List<RecentItem> {
        val json = prefs.getString("recent_personas", "[]") ?: "[]"
        val type = object : TypeToken<List<RecentItem>>() {}.type
        return gson.fromJson(json, type)
    }
    fun addRecentPersona(seriesId: String, gameId: String, personaName: String) {
        val recents = getRecentPersonas().toMutableList()
        val item = RecentItem(seriesId, gameId, personaName, System.currentTimeMillis())
        recents.removeAll { it.name == personaName && it.gameId == gameId }
        recents.add(0, item)
        if (recents.size > 50) recents.removeLast()
        prefs.edit().putString("recent_personas", gson.toJson(recents)).apply()
    }
    
    // Recently viewed - Enemies
    fun getRecentEnemies(): List<RecentItem> {
        val json = prefs.getString("recent_enemies", "[]") ?: "[]"
        val type = object : TypeToken<List<RecentItem>>() {}.type
        return gson.fromJson(json, type)
    }
    fun addRecentEnemy(seriesId: String, gameId: String, enemyName: String) {
        val recents = getRecentEnemies().toMutableList()
        val item = RecentItem(seriesId, gameId, enemyName, System.currentTimeMillis())
        recents.removeAll { it.name == enemyName && it.gameId == gameId }
        recents.add(0, item)
        if (recents.size > 50) recents.removeLast()
        prefs.edit().putString("recent_enemies", gson.toJson(recents)).apply()
    }
    
    // P3P Protagonist selection (Male MC or FeMC)
    enum class P3PProtagonist {
        MALE,
        FEMC
    }
    
    fun getP3PProtagonist(): P3PProtagonist {
        val value = prefs.getString("p3p_protagonist", "MALE") ?: "MALE"
        return try {
            P3PProtagonist.valueOf(value)
        } catch (e: Exception) {
            P3PProtagonist.MALE
        }
    }
    
    fun setP3PProtagonist(protagonist: P3PProtagonist) {
        prefs.edit().putString("p3p_protagonist", protagonist.name).apply()
    }
    
    data class RecentItem(
        val seriesId: String,
        val gameId: String,
        val name: String,
        val timestamp: Long
    )
}
