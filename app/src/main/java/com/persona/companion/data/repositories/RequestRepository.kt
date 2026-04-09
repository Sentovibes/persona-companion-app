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

    suspend fun syncRequestsIfNeeded(gameId: String, requestPath: String) = withContext(Dispatchers.IO) {
        try {
            val count = requestDao.getRequestCount(gameId)
            if (count == 0 && requestPath.isNotEmpty()) {
                Log.d("RequestRepository", "Syncing requests for $gameId from $requestPath")
                val jsonRequests = JsonLoader.loadRequests(context, requestPath)
                val entities = jsonRequests.map { req ->
                    RequestEntity(
                        id = req.id,
                        name = req.name,
                        reward = req.reward,
                        giver = req.giver,
                        quest_giver = req.quest_giver,
                        deadline = req.deadline,
                        location = req.location,
                        category = req.category,
                        available = req.available,
                        intel_required = req.intel_required,
                        target_name = req.target_name,
                        target_enemy = req.target_enemy,
                        remarks = req.remarks,
                        difficulty = req.difficulty,
                        gameId = gameId,
                        isCompleted = false
                    )
                }
                requestDao.insertAll(entities)
                Log.d("RequestRepository", "Synced ${entities.size} requests to DB")
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
