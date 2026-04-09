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

    suspend fun getItems(gameId: String, itemPath: String): List<Item> = withContext(Dispatchers.IO) {
        try {
            Log.d("ItemRepository", "Loading items for $gameId from $itemPath")
            
            // 1. Check if we already have items for this game in DB
            val count = itemDao.getItemCount(gameId)
            Log.d("ItemRepository", "Found $count items in DB for $gameId")
            
            if (count > 0) {
                return@withContext itemDao.getItemsForGameSync(gameId)
            }

            // 2. Otherwise load from JSON
            Log.d("ItemRepository", "Syncing items from JSON for $gameId...")
            val jsonString = context.assets.open(itemPath).bufferedReader().use { it.readText() }
            val data = gson.fromJson(jsonString, ItemData::class.java)
            
            Log.d("ItemRepository", "Parsed ${data.items.size} items from JSON")
            
            // 3. Tag with gameId and save to DB
            // We ensure id is not set so Room autogenerates
            val itemsToSave = data.items.map { it.copy(id = 0, gameId = gameId) }
            itemDao.insertAll(itemsToSave)

            Log.d("ItemRepository", "Successfully saved ${itemsToSave.size} items to DB for $gameId")
            itemsToSave.sortedBy { it.name }
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error loading items for $gameId", e)
            emptyList()
        }
    }
}
