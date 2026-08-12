package com.persona.companion.data.repositories

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.persona.companion.BuildConfig
import com.persona.companion.data.database.AppDatabase
import com.persona.companion.models.Item
import com.persona.companion.models.ItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val context: Context) {
    private val gson = Gson()
    private val database = AppDatabase.getDatabase(context)
    private val itemDao = database.itemDao()
    private val syncPrefs = context.getSharedPreferences("data_sync", Context.MODE_PRIVATE)

    suspend fun getItems(gameId: String, itemPath: String, aigisItemPath: String? = null): List<Item> = withContext(Dispatchers.IO) {
        try {
            Log.d("ItemRepository", "Loading items for $gameId from $itemPath")

            // Re-sync from JSON when the table is empty or the app was updated,
            // so data fixes shipped in an update reach installs that already synced.
            val syncKey = "items_synced_version_$gameId"
            val needsSync = itemDao.getItemCount(gameId) == 0 ||
                syncPrefs.getInt(syncKey, -1) != BuildConfig.VERSION_CODE

            if (needsSync && itemPath.isNotEmpty()) {
                Log.d("ItemRepository", "Syncing items from JSON for $gameId...")
                val itemsToSave = mutableListOf<Item>()

                val jsonString = context.assets.open(itemPath).bufferedReader().use { it.readText() }
                val data = gson.fromJson(jsonString, ItemData::class.java)
                itemsToSave += data.items.map { it.copy(id = 0, gameId = gameId, episodeAigis = false) }

                if (!aigisItemPath.isNullOrEmpty()) {
                    val aigisJson = context.assets.open(aigisItemPath).bufferedReader().use { it.readText() }
                    val aigisData = gson.fromJson(aigisJson, ItemData::class.java)
                    itemsToSave += aigisData.items.map { it.copy(id = 0, gameId = gameId, episodeAigis = true) }
                }

                // Parse fully before touching the table so a bad asset never wipes existing data
                itemDao.deleteForGame(gameId)
                itemDao.insertAll(itemsToSave)
                syncPrefs.edit().putInt(syncKey, BuildConfig.VERSION_CODE).apply()
            }

            itemDao.getItemsForGameSync(gameId).sortedBy { it.name }
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error loading items for $gameId", e)
            emptyList()
        }
    }
}
