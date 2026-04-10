package com.persona.companion.data.repositories

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.persona.companion.data.database.AppDatabase
import com.persona.companion.models.Item
import com.persona.companion.models.ItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val context: Context) {
    private val gson = Gson()
    private val database = AppDatabase.getDatabase(context)
    private val itemDao = database.itemDao()

    suspend fun getItems(gameId: String, itemPath: String, aigisItemPath: String? = null): List<Item> = withContext(Dispatchers.IO) {
        try {
            Log.d("ItemRepository", "Loading items for $gameId from $itemPath")
            
            // 1. Sync base items if needed
            val count = itemDao.getItemCount(gameId)
            if (count == 0 && itemPath.isNotEmpty()) {
                Log.d("ItemRepository", "Syncing base items from JSON for $gameId...")
                val jsonString = context.assets.open(itemPath).bufferedReader().use { it.readText() }
                val data = gson.fromJson(jsonString, ItemData::class.java)
                val itemsToSave = data.items.map { it.copy(id = 0, gameId = gameId, episodeAigis = false) }
                itemDao.insertAll(itemsToSave)
            }

            // 2. Sync Aigis items if needed
            if (!aigisItemPath.isNullOrEmpty()) {
                val aigisCount = itemDao.getItemsForGameSync(gameId).count { it.episodeAigis == true }
                if (aigisCount == 0) {
                    Log.d("ItemRepository", "Syncing Aigis items from JSON for $gameId...")
                    val jsonString = context.assets.open(aigisItemPath).bufferedReader().use { it.readText() }
                    val data = gson.fromJson(jsonString, ItemData::class.java)
                    val itemsToSave = data.items.map { it.copy(id = 0, gameId = gameId, episodeAigis = true) }
                    itemDao.insertAll(itemsToSave)
                }
            }

            // 3. Return all items for this game (filtering will happen in ViewModel)
            itemDao.getItemsForGameSync(gameId).sortedBy { it.name }
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error loading items for $gameId", e)
            emptyList()
        }
    }
}
