package com.persona.companion.fusion

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SpecialFusionLoader {
    
    /**
     * Load special fusion recipes for a game
     * Returns a map of persona name to list of ingredient lists
     */
    fun loadSpecialFusions(context: Context, gameId: String): Map<String, List<List<String>>> {
        val specialPath = when (gameId) {
            "p3fes", "p3p" -> "data/special-fusions/p3-special.json"
            "p3r" -> "data/special-fusions/p3r-special.json"
            "p4", "p4g" -> "data/special-fusions/p4-special.json"
            "p5" -> "data/special-fusions/p5-special.json"
            "p5r" -> "data/special-fusions/p5r-special.json"
            else -> return emptyMap()
        }
        
        return try {
            val json = context.assets.open(specialPath).bufferedReader().use { it.readText() }
            Gson().fromJson(json, object : TypeToken<Map<String, List<List<String>>>>() {}.type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
