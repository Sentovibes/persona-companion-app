package com.persona.companion.data

import android.content.Context
import android.util.Log
import com.persona.companion.models.Persona
import com.persona.companion.utils.JsonLoader

private const val TAG = "PersonaRepository"

class PersonaRepository(private val context: Context) {

    private val cache = mutableMapOf<String, List<Persona>>()

    fun getPersonas(dataPath: String): List<Persona> {
        Log.d(TAG, "getPersonas() called with dataPath: $dataPath")
        return cache.getOrPut(dataPath) {
            Log.d(TAG, "Cache miss, loading from JSON")
            val personas = JsonLoader.loadPersonas(context, dataPath)
            Log.d(TAG, "Loaded and cached ${personas.size} personas")
            personas
        }.also {
            Log.d(TAG, "Returning ${it.size} personas from cache")
        }
    }

    fun getPersonaByName(dataPath: String, name: String): Persona? {
        return getPersonas(dataPath).find { it.name == name }
    }

    fun getArcanaList(dataPath: String): List<String> {
        return getPersonas(dataPath).mapNotNull { it.arcana }.distinct().sorted()
    }

    fun searchPersonas(dataPath: String, query: String): List<Persona> {
        if (query.isBlank()) return getPersonas(dataPath)
        val lower = query.lowercase()
        return getPersonas(dataPath).filter {
            it.name.lowercase().contains(lower) || (it.arcana?.lowercase()?.contains(lower) == true)
        }
    }
}