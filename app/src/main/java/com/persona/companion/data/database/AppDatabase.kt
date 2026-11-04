package com.persona.companion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.persona.companion.models.Item
import com.persona.companion.models.RequestEntity

@Database(entities = [Item::class, RequestEntity::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun requestDao(): RequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_requests_gameId_sortOrder ON requests(gameId, sortOrder)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_requests_gameId_name ON requests(gameId, name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_items_gameId_name ON items(gameId, name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_items_gameId_category ON items(gameId, category)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "persona_database"
                )
                .addMigrations(MIGRATION_8_9)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
