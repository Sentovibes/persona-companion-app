package com.persona.companion.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.persona.companion.models.Persona

private const val TAG = "JsonLoader"

/**
 * Loads Persona data from JSON files stored in the app's assets folder.
 *
 * All JSON files live under: assets/data/<series>/<file>.json
 *
 * To add new data simply drop a properly formatted JSON file in that directory
 * and reference its path in [SeriesData].
 */
object JsonLoader {

    private val gson = Gson()

    /**
     * Loads a list of [Persona] objects from a JSON file at [path] inside assets.
     * Returns an empty list if the file doesn't exist or is malformed.
     */
    fun loadPersonas(context: Context, path: String): List<Persona> {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Persona>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Could not load personas from '$path': ${e.message}")
            emptyList()
        }
    }
}
