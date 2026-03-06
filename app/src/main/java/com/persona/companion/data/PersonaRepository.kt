package com.persona.companion.data

import android.content.Context
import com.persona.companion.models.Persona
import com.persona.companion.utils.JsonLoader

/**
 * Repository that loads and caches Persona data.
 *
 * Each unique [dataPath] is loaded once and then held in memory for the
 * lifetime of this object. In a larger app you might scope this to a
 * ViewModel, but for now the companion object cache keeps things simple.
 */
class PersonaRepository(private val context: Context) {

    private val cache = mutableMapOf<String, List<Persona>>()

    /** Returns all personas for the given JSON data path. */
    fun getPersonas(dataPath: String): List<Persona> {
        return cache.getOrPut(dataPath) {
            JsonLoader.loadPersonas(context, dataPath)
        }
    }

    /** Finds a single persona by exact name match. */
    fun getPersonaByName(dataPath: String, name: String): Persona? {
        return getPersonas(dataPath).find { it.name == name }
    }

    /** Returns all distinct arcana names in alphabetical order. */
    fun getArcanaList(dataPath: String): List<String> {
        return getPersonas(dataPath).map { it.arcana }.distinct().sorted()
    }

    /** Filters the list by a free-text query matching name or arcana. */
    fun searchPersonas(dataPath: String, query: String): List<Persona> {
        if (query.isBlank()) return getPersonas(dataPath)
        val lower = query.lowercase()
        return getPersonas(dataPath).filter {
            it.name.lowercase().contains(lower) || it.arcana.lowercase().contains(lower)
        }
    }
}
