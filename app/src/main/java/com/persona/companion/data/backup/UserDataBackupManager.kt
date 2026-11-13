package com.persona.companion.data.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.persona.companion.BuildConfig
import com.persona.companion.data.AppPreferences
import com.persona.companion.data.UserPreferences
import com.persona.companion.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupPayload(
    @SerializedName("schemaVersion") val schemaVersion: Int = 1,
    @SerializedName("appVersion") val appVersion: String = BuildConfig.VERSION_NAME,
    @SerializedName("versionCode") val versionCode: Int = BuildConfig.VERSION_CODE,
    @SerializedName("exportTimestamp") val exportTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("favoritePersonas") val favoritePersonas: Set<String> = emptySet(),
    @SerializedName("favoriteEnemies") val favoriteEnemies: Set<String> = emptySet(),
    @SerializedName("recentPersonas") val recentPersonas: List<UserPreferences.RecentItem> = emptyList(),
    @SerializedName("recentEnemies") val recentEnemies: List<UserPreferences.RecentItem> = emptyList(),
    @SerializedName("completedRequests") val completedRequests: List<CompletedRequestEntry> = emptyList(),
    @SerializedName("pinnedSocialLinks") val pinnedSocialLinks: Set<String> = emptySet(),
    @SerializedName("maxedSocialLinks") val maxedSocialLinks: Set<String> = emptySet(),
    @SerializedName("preferences") val preferences: Map<String, String> = emptyMap()
)

data class CompletedRequestEntry(
    @SerializedName("gameId") val gameId: String,
    @SerializedName("name") val name: String,
    @SerializedName("isCompleted") val isCompleted: Boolean
)

object UserDataBackupManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackupPayload(context: Context): BackupPayload = withContext(Dispatchers.IO) {
        val userPrefs = UserPreferences(context)
        val appPrefs = AppPreferences(context)
        val appSettings = appPrefs.getSettings()
        val db = AppDatabase.getDatabase(context)

        val dbCompleted = db.requestDao().getAllCompletedRequests().map {
            CompletedRequestEntry(it.gameId, it.name, it.isCompleted)
        }
        val dbKeys = dbCompleted.map { "${it.gameId}\u0000${it.name}" }.toSet()
        val syncPrefs = context.getSharedPreferences("data_sync", Context.MODE_PRIVATE)
        val pendingCompleted = mutableListOf<CompletedRequestEntry>()
        syncPrefs.all.forEach { (key, value) ->
            if (key.startsWith("pending_completed_requests_") && value is Set<*>) {
                val gameId = key.removePrefix("pending_completed_requests_")
                value.filterIsInstance<String>().forEach { reqName ->
                    if ("$gameId\u0000$reqName" !in dbKeys) {
                        pendingCompleted += CompletedRequestEntry(gameId, reqName, true)
                    }
                }
            }
        }

        val prefsMap = mapOf(
            "p3p_protagonist" to userPrefs.getP3PProtagonist().name,
            "no_spoilers_mode" to userPrefs.isNoSpoilersMode().toString(),
            "default_game_p3" to userPrefs.getDefaultGame("p3"),
            "default_game_p4" to userPrefs.getDefaultGame("p4"),
            "default_game_p5" to userPrefs.getDefaultGame("p5"),
            "show_dlc" to appSettings.showDlc.toString(),
            "show_episode_aigis" to appSettings.showEpisodeAigis.toString()
        )

        BackupPayload(
            favoritePersonas = userPrefs.getFavoritePersonas(),
            favoriteEnemies = userPrefs.getFavoriteEnemies(),
            recentPersonas = userPrefs.getRecentPersonas(),
            recentEnemies = userPrefs.getRecentEnemies(),
            completedRequests = dbCompleted + pendingCompleted,
            pinnedSocialLinks = userPrefs.getPinnedSocialLinks(),
            maxedSocialLinks = userPrefs.getMaxedSocialLinks(),
            preferences = prefsMap
        )
    }

    suspend fun exportToFile(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val payload = createBackupPayload(context)
            val jsonString = gson.toJson(payload)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("Could not open destination file"))
            Result.success(
                payload.favoritePersonas.size +
                    payload.favoriteEnemies.size +
                    payload.completedRequests.size +
                    payload.pinnedSocialLinks.size +
                    payload.maxedSocialLinks.size
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromFile(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val rootObj: JsonObject = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val parsed = JsonParser.parseReader(reader)
                    if (parsed.isJsonObject) parsed.asJsonObject else null
                }
            } ?: return@withContext Result.failure(Exception("Could not read backup JSON"))

            val userPrefs = UserPreferences(context)
            val appPrefs = AppPreferences(context)
            val db = AppDatabase.getDatabase(context)
            var restoredCount = 0

            rootObj.getAsJsonArray("favoritePersonas")?.forEach { elem ->
                normalizeFavoriteId(elem)?.let {
                    userPrefs.addFavoritePersona(it)
                    restoredCount++
                }
            }

            rootObj.getAsJsonArray("favoriteEnemies")?.forEach { elem ->
                normalizeFavoriteId(elem)?.let {
                    userPrefs.addFavoriteEnemy(it)
                    restoredCount++
                }
            }

            parseRecentItems(rootObj.getAsJsonArray("recentPersonas")).asReversed().forEach { item ->
                userPrefs.addRecentPersona(item.seriesId, item.gameId, item.name)
            }

            parseRecentItems(rootObj.getAsJsonArray("recentEnemies")).asReversed().forEach { item ->
                userPrefs.addRecentEnemy(item.seriesId, item.gameId, item.name)
            }

            rootObj.getAsJsonArray("pinnedSocialLinks")?.forEach { elem ->
                if (elem.isJsonPrimitive && elem.asString.isNotBlank()) {
                    userPrefs.addPinnedSocialLink(elem.asString)
                    restoredCount++
                }
            }

            rootObj.getAsJsonArray("maxedSocialLinks")?.forEach { elem ->
                if (elem.isJsonPrimitive && elem.asString.isNotBlank()) {
                    userPrefs.addMaxedSocialLink(elem.asString)
                    restoredCount++
                }
            }

            rootObj.getAsJsonObject("preferences")?.let { prefsObj ->
                readStringPref(prefsObj, "p3p_protagonist", "p3pProtagonist")?.let { value ->
                    try {
                        userPrefs.setP3PProtagonist(UserPreferences.P3PProtagonist.valueOf(value.uppercase()))
                    } catch (_: Exception) {}
                }
                readBoolPref(prefsObj, "no_spoilers_mode", "noSpoilersMode")?.let { enabled ->
                    userPrefs.setNoSpoilersMode(enabled)
                }
                readStringPref(prefsObj, "default_game_p3", "defaultGame_p3")?.let { userPrefs.setDefaultGame("p3", it) }
                readStringPref(prefsObj, "default_game_p4", "defaultGame_p4")?.let { userPrefs.setDefaultGame("p4", it) }
                readStringPref(prefsObj, "default_game_p5", "defaultGame_p5")?.let { userPrefs.setDefaultGame("p5", it) }
                readBoolPref(prefsObj, "show_dlc", "showDlc")?.let { appPrefs.setShowDlc(it) }
                readBoolPref(prefsObj, "show_episode_aigis", "showEpisodeAigis")?.let { appPrefs.setShowEpisodeAigis(it) }
            }

            val syncPrefs = context.getSharedPreferences("data_sync", Context.MODE_PRIVATE)
            val pendingByGame = mutableMapOf<String, MutableSet<String>>()
            rootObj.getAsJsonArray("completedRequests")?.forEach { elem ->
                if (elem.isJsonObject) {
                    val obj = elem.asJsonObject
                    val gameId = obj.get("gameId")?.takeIf { it.isJsonPrimitive }?.asString?.trim() ?: return@forEach
                    val name = obj.get("name")?.takeIf { it.isJsonPrimitive }?.asString?.trim() ?: return@forEach
                    if (name.isEmpty()) return@forEach
                    val completed = obj.get("isCompleted")?.takeIf { it.isJsonPrimitive }?.asBoolean ?: true

                    if (db.requestDao().getRequestCount(gameId) > 0) {
                        db.requestDao().updateCompletion(name, gameId, completed)
                    } else {
                        val set = pendingByGame.getOrPut(gameId) {
                            (syncPrefs.getStringSet("pending_completed_requests_$gameId", emptySet()) ?: emptySet()).toMutableSet()
                        }
                        if (completed) set.add(name) else set.remove(name)
                    }
                    restoredCount++
                }
            }
            if (pendingByGame.isNotEmpty()) {
                val editor = syncPrefs.edit()
                pendingByGame.forEach { (gameId, names) ->
                    editor.putStringSet("pending_completed_requests_$gameId", names)
                }
                editor.apply()
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun inferSeriesId(gameId: String): String = when {
        gameId.startsWith("p3") -> "p3"
        gameId.startsWith("p4") -> "p4"
        gameId.startsWith("p5") -> "p5"
        else -> "p5"
    }

    private fun normalizeFavoriteId(elem: JsonElement): String? {
        if (elem.isJsonPrimitive) {
            val raw = elem.asString.trim()
            if (raw.isEmpty()) return null
            val parts = raw.split("_", limit = 3)
            if (parts.size >= 3 && parts[0] in setOf("p3", "p4", "p5")) return raw
            if (parts.size >= 2) {
                val gameId = parts[0]
                return "${inferSeriesId(gameId)}_$raw"
            }
            return raw
        }
        if (elem.isJsonObject) {
            val obj = elem.asJsonObject
            val gameId = obj.get("gameId")?.takeIf { it.isJsonPrimitive }?.asString?.trim() ?: return null
            val itemName = (obj.get("personaName") ?: obj.get("enemyName") ?: obj.get("name"))
                ?.takeIf { it.isJsonPrimitive }?.asString?.trim() ?: return null
            if (gameId.isEmpty() || itemName.isEmpty()) return null
            return "${inferSeriesId(gameId)}_${gameId}_$itemName"
        }
        return null
    }

    private fun parseRecentItems(array: com.google.gson.JsonArray?): List<UserPreferences.RecentItem> {
        if (array == null) return emptyList()
        return array.mapNotNull { elem ->
            if (!elem.isJsonObject) return@mapNotNull null
            val obj = elem.asJsonObject
            val gameId = obj.get("gameId")?.takeIf { it.isJsonPrimitive }?.asString?.trim() ?: return@mapNotNull null
            val name = obj.get("name")?.takeIf { it.isJsonPrimitive }?.asString?.trim() ?: return@mapNotNull null
            val seriesId = obj.get("seriesId")?.takeIf { it.isJsonPrimitive }?.asString?.trim()
                ?.takeIf { it.isNotEmpty() } ?: inferSeriesId(gameId)
            val timestamp = obj.get("timestamp")?.takeIf { it.isJsonPrimitive }?.asLong ?: System.currentTimeMillis()
            if (gameId.isEmpty() || name.isEmpty()) return@mapNotNull null
            UserPreferences.RecentItem(seriesId, gameId, name, timestamp)
        }.take(50)
    }

    private fun readStringPref(prefsObj: JsonObject, vararg keys: String): String? {
        for (k in keys) {
            val elem = prefsObj.get(k) ?: continue
            if (elem.isJsonPrimitive) return elem.asString
        }
        return null
    }

    private fun readBoolPref(prefsObj: JsonObject, vararg keys: String): Boolean? {
        for (k in keys) {
            val elem = prefsObj.get(k) ?: continue
            if (elem.isJsonPrimitive) {
                val prim = elem.asJsonPrimitive
                if (prim.isBoolean) return prim.asBoolean
                if (prim.isString) return prim.asString.equals("true", ignoreCase = true)
            }
        }
        return null
    }
}
