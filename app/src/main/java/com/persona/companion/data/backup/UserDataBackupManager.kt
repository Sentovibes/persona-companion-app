package com.persona.companion.data.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.persona.companion.BuildConfig
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
        val db = AppDatabase.getDatabase(context)

        val completedList = db.requestDao().getAllCompletedRequests().map {
            CompletedRequestEntry(it.gameId, it.name, it.isCompleted)
        }

        val prefsMap = mapOf(
            "p3p_protagonist" to userPrefs.getP3PProtagonist().name,
            "no_spoilers_mode" to userPrefs.isNoSpoilersMode().toString(),
            "default_game_p3" to userPrefs.getDefaultGame("p3"),
            "default_game_p4" to userPrefs.getDefaultGame("p4"),
            "default_game_p5" to userPrefs.getDefaultGame("p5")
        )

        BackupPayload(
            favoritePersonas = userPrefs.getFavoritePersonas(),
            favoriteEnemies = userPrefs.getFavoriteEnemies(),
            recentPersonas = userPrefs.getRecentPersonas(),
            recentEnemies = userPrefs.getRecentEnemies(),
            completedRequests = completedList,
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
            Result.success(payload.favoritePersonas.size + payload.completedRequests.size + payload.pinnedSocialLinks.size + payload.maxedSocialLinks.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromFile(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val payload: BackupPayload = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    gson.fromJson(reader, BackupPayload::class.java)
                }
            } ?: return@withContext Result.failure(Exception("Could not read source file"))

            val userPrefs = UserPreferences(context)
            val db = AppDatabase.getDatabase(context)

            // Restore favorites and social links state (null-safe for older v1 backups)
            (payload.favoritePersonas ?: emptySet()).forEach { userPrefs.addFavoritePersona(it) }
            (payload.favoriteEnemies ?: emptySet()).forEach { userPrefs.addFavoriteEnemy(it) }
            (payload.pinnedSocialLinks ?: emptySet()).forEach { userPrefs.addPinnedSocialLink(it) }
            (payload.maxedSocialLinks ?: emptySet()).forEach { userPrefs.addMaxedSocialLink(it) }

            // Restore preferences
            payload.preferences["p3p_protagonist"]?.let {
                try {
                    userPrefs.setP3PProtagonist(UserPreferences.P3PProtagonist.valueOf(it))
                } catch (_: Exception) {}
            }
            payload.preferences["no_spoilers_mode"]?.let {
                userPrefs.setNoSpoilersMode(it.toBoolean())
            }
            payload.preferences["default_game_p3"]?.let { userPrefs.setDefaultGame("p3", it) }
            payload.preferences["default_game_p4"]?.let { userPrefs.setDefaultGame("p4", it) }
            payload.preferences["default_game_p5"]?.let { userPrefs.setDefaultGame("p5", it) }

            // Restore completed requests in Room DB
            payload.completedRequests.forEach { req ->
                db.requestDao().updateCompletion(req.name, req.gameId, req.isCompleted)
            }

            val totalRestored = (payload.favoritePersonas?.size ?: 0) + (payload.completedRequests?.size ?: 0) + (payload.pinnedSocialLinks?.size ?: 0) + (payload.maxedSocialLinks?.size ?: 0)
            Result.success(totalRestored)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
