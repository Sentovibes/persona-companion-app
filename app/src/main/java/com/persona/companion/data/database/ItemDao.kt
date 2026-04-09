package com.persona.companion.data.database

import androidx.room.*
import com.persona.companion.models.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE gameId = :gameId ORDER BY name ASC")
    fun getItemsForGame(gameId: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE gameId = :gameId ORDER BY name ASC")
    suspend fun getItemsForGameSync(gameId: String): List<Item>

    @Query("SELECT * FROM items WHERE gameId = :gameId AND category = :category ORDER BY name ASC")
    fun getItemsByCategory(gameId: String, category: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE gameId = :gameId AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchItems(gameId: String, query: String): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Query("DELETE FROM items WHERE gameId = :gameId")
    suspend fun deleteForGame(gameId: String)

    @Query("SELECT COUNT(*) FROM items WHERE gameId = :gameId")
    suspend fun getItemCount(gameId: String): Int
}
