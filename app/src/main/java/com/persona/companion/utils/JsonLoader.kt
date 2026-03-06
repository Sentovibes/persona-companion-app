package com.persona.companion.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.persona.companion.models.Persona

private const val TAG = "JsonLoader"

object JsonLoader {
    private val gson = Gson()

    fun loadPersonas(context: Context, path: String): List<Persona> {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            
            val type = object : TypeToken<Map<String, Persona>>() {}.type
            val personaMap: Map<String, Persona> = gson.fromJson(json, type) ?: emptyMap()
            
            personaMap.map { (name, persona) ->
                persona.copy(name = name)
            }.sortedBy { it.level ?: 0 }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load personas from '$path': ${e.message}")
            emptyList()
        }
    }
}