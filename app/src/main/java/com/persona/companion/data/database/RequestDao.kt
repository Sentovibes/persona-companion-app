package com.persona.companion.data.database

import androidx.room.*
import com.persona.companion.models.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests WHERE gameId = :gameId ORDER BY name ASC")
    fun getRequestsForGame(gameId: String): Flow<List<RequestEntity>>

    @Query("SELECT COUNT(*) FROM requests WHERE gameId = :gameId")
    suspend fun getRequestCount(gameId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<RequestEntity>)

    @Query("UPDATE requests SET isCompleted = :completed WHERE name = :name AND gameId = :gameId")
    suspend fun updateCompletion(name: String, gameId: String, completed: Boolean)

    @Query("DELETE FROM requests WHERE gameId = :gameId")
    suspend fun deleteForGame(gameId: String)
}
