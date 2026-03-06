package com.persona.companion.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.persona.companion.models.Persona

private const val TAG = "JsonLoader"

object JsonLoader {
    private val gson = Gson()

    fun loadPersonas(context: Context, path: String): List<Persona> {
        return try {
            Log.d(TAG, "Loading personas from: $path")
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            Log.d(TAG, "JSON loaded, length: ${json.length}")
            
            // Try to detect if it's an array or map format
            val trimmed = json.trim()
            val personas = if (trimmed.startsWith("[")) {
                // Array format (P3 Reload style)
                Log.d(TAG, "Detected array format")
                val type = object : TypeToken<List<Persona>>() {}.type
                val personaList: List<Persona> = gson.fromJson(json, type) ?: emptyList()
                Log.d(TAG, "Parsed ${personaList.size} personas from array")
                personaList.sortedBy { it.level ?: 0 }
            } else {
                // Map format (traditional style)
                Log.d(TAG, "Detected map format")
                val type = object : TypeToken<Map<String, Persona>>() {}.type
                val personaMap: Map<String, Persona> = gson.fromJson(json, type) ?: emptyMap()
                Log.d(TAG, "Parsed ${personaMap.size} personas from map")
                
                personaMap.map { (name, persona) ->
                    persona.copy(name = name)
                }.sortedBy { it.level ?: 0 }
            }
            
            Log.d(TAG, "Returning ${personas.size} personas")
            personas
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON syntax error in '$path': ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Could not load personas from '$path': ${e.message}", e)
            emptyList()
        }
    }
}