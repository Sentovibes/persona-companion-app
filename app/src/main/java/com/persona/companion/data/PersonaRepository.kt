package com.persona.companion.data

import android.content.Context
import com.persona.companion.models.Persona
import com.persona.companion.utils.JsonLoader

class PersonaRepository(private val context: Context) {

    private val cache = mutableMapOf<String, List<Persona>>()

    fun getPersonas(dataPath: String): List<Persona> {
        return cache.getOrPut(dataPath) {
            JsonLoader.loadPersonas(context, dataPath)
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