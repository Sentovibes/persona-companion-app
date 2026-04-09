package com.persona.companion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.persona.companion.models.Item
import com.persona.companion.models.Persona
import com.persona.companion.models.Enemy
import com.persona.companion.models.RequestEntity

@Database(entities = [Item::class, RequestEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun requestDao(): RequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "persona_database"
                )
                .fallbackToDestructiveMigration() // Reset DB on schema change for now
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
