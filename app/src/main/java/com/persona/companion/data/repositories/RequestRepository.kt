package com.persona.companion.data.repositories

import android.content.Context
import android.util.Log
import com.persona.companion.data.database.RequestDao
import com.persona.companion.models.RequestEntity
import com.persona.companion.utils.JsonLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RequestRepository(
    private val context: Context,
    private val requestDao: RequestDao
) {
    fun getRequests(gameId: String): Flow<List<RequestEntity>> {
        return requestDao.getRequestsForGame(gameId)
    }

    fun getCompletedCount(gameId: String): Flow<Int> {
        return requestDao.getCompletedRequestCount(gameId)
    }

    fun getTotalCount(gameId: String): Flow<Int> {
        return requestDao.getTotalRequestCount(gameId)
    }

    suspend fun syncRequestsIfNeeded(gameId: String, requestPath: String, aigisRequestPath: String? = null) = withContext(Dispatchers.IO) {
        try {
            // Re-sync from JSON when the table is empty or the app was updated,
            // so data fixes shipped in an update reach installs that already synced.
            val syncPrefs = context.getSharedPreferences("data_sync", Context.MODE_PRIVATE)
            val syncKey = "requests_synced_version_$gameId"
            val needsSync = requestDao.getRequestCount(gameId) == 0 ||
                syncPrefs.getInt(syncKey, -1) != com.persona.companion.BuildConfig.VERSION_CODE

            if (needsSync && requestPath.isNotEmpty()) {
                Log.d("RequestRepository", "Syncing requests for $gameId from $requestPath")
                val baseJson = JsonLoader.loadRequests(context, requestPath)
                val aigisJson = if (!aigisRequestPath.isNullOrEmpty()) {
                    JsonLoader.loadRequests(context, aigisRequestPath)
                } else emptyList()

                if (baseJson.isNotEmpty() || aigisJson.isNotEmpty()) {
                    // Completion is user state living in the same table — carry it across the resync by name
                    val completedNames = requestDao.getRequestsForGameSync(gameId)
                        .filter { it.isCompleted }
                        .map { it.name }
                        .toSet()

                    fun toEntity(req: com.persona.companion.models.Request, sortOrder: Int, aigis: Boolean): RequestEntity {
                        val name = req.name ?: "Unknown Request"
                        return RequestEntity(
                            id = req.id,
                            name = name,
                            reward = req.reward ?: "-",
                            giver = req.giver,
                            quest_giver = req.quest_giver,
                            deadline = req.deadline,
                            location = req.location,
                            category = req.category,
                            available = req.available ?: "-",
                            intel_required = req.intel_required,
                            target_name = req.target_name,
                            target_enemy = req.target_enemy,
                            remarks = req.remarks,
                            difficulty = req.difficulty,
                            gameId = gameId,
                            sortOrder = sortOrder,
                            isCompleted = name in completedNames,
                            episodeAigis = aigis
                        )
                    }

                    val entities =
                        baseJson.mapIndexed { index, req -> toEntity(req, req.sortOrder ?: index, aigis = false) } +
                        aigisJson.mapIndexed { index, req -> toEntity(req, req.sortOrder ?: (1000 + index), aigis = true) }

                    requestDao.deleteForGame(gameId)
                    requestDao.insertAll(entities)
                    syncPrefs.edit().putInt(syncKey, com.persona.companion.BuildConfig.VERSION_CODE).apply()
                    Log.d("RequestRepository", "Synced ${entities.size} requests to DB (${aigisJson.size} Aigis)")
                }
            }
        } catch (e: Exception) {
            Log.e("RequestRepository", "Error syncing requests for $gameId", e)
        }
        Unit
    }

    suspend fun toggleCompletion(name: String, gameId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        requestDao.updateCompletion(name, gameId, completed)
    }
}
