package com.persona.companion.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.persona.companion.models.*

private const val TAG = "JsonLoader"

object JsonLoader {
    private val gson = Gson()

    fun loadPersonas(context: Context, path: String): List<Persona> {
        return try {
            Log.d(TAG, "Loading personas from stream: $path")
            val jsonElement = context.assets.open(path).bufferedReader().use { reader ->
                JsonParser.parseReader(reader)
            }

            val personas = if (jsonElement.isJsonArray) {
                val type = TypeToken.getParameterized(List::class.java, Persona::class.java).type
                val personaList: List<Persona> = gson.fromJson(jsonElement, type) ?: emptyList()
                personaList.sortedBy { it.level ?: 0 }
            } else if (jsonElement.isJsonObject) {
                val type = TypeToken.getParameterized(Map::class.java, String::class.java, Persona::class.java).type
                val personaMap: Map<String, Persona> = gson.fromJson(jsonElement, type) ?: emptyMap()
                personaMap.map { (name, persona) ->
                    persona.copy(name = name)
                }.sortedBy { it.level ?: 0 }
            } else {
                emptyList()
            }

            if (personas.isEmpty()) {
                throw Exception("Parsed 0 personas from $path")
            }
            personas
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON syntax error in '$path': ${e.message}", e)
            throw Exception("JSON syntax error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "General error loading personas from '$path': ${e.message}", e)
            throw e
        }
    }

    fun loadEnemies(context: Context, path: String): List<Enemy> {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, Enemy::class.java).type
                val enemies: List<Enemy> = gson.fromJson(reader, type) ?: emptyList()
                enemies.sortedBy { it.level }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading enemies from '$path': ${e.message}", e)
            emptyList()
        }
    }

    fun loadBosses(context: Context, path: String): BossData {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                gson.fromJson(reader, BossData::class.java) ?: BossData(emptyList(), emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bosses from '$path': ${e.message}", e)
            BossData(emptyList(), emptyList())
        }
    }

    fun loadRequests(context: Context, path: String): List<Request> {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, Request::class.java).type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading requests from '$path': ${e.message}", e)
            emptyList()
        }
    }

    fun loadNewsUpdates(context: Context, path: String): List<NewsUpdate> {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, NewsUpdate::class.java).type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading news updates from '$path': ${e.message}", e)
            emptyList()
        }
    }

    fun loadQuestGuides(context: Context, path: String): List<QuestGuideGiver> {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, QuestGuideGiver::class.java).type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quest guides from '$path': ${e.message}", e)
            emptyList()
        }
    }

    fun loadBossGuides(context: Context, path: String): List<BossGuideGame> {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, BossGuideGame::class.java).type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading boss guides from '$path': ${e.message}", e)
            emptyList()
        }
    }

    fun loadDayGuides(context: Context, path: String): List<DayGuideGame> {
        return try {
            context.assets.open(path).bufferedReader().use { reader ->
                val type = TypeToken.getParameterized(List::class.java, DayGuideGame::class.java).type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading day guides from '$path': ${e.message}", e)
            emptyList()
        }
    }
}
