package com.alfred.kitabalhuda.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alfred.kitabalhuda.database.dao.*
import com.alfred.kitabalhuda.database.entity.*
import java.util.concurrent.Executors

@Database(
    entities = [
        SourateEntity::class,
        ReciteurEntity::class,
        AudioEntity::class,
        HadithEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        ListeningHistoryEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun reciteurDao(): ReciteurDao
    abstract fun sourateDao(): SourateDao
    abstract fun audioDao(): AudioDao
    abstract fun hadithDao(): HadithDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun listeningHistoryDao(): ListeningHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kitab_alhuda_database" // Renamed database
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration() // Only for versions we don't have migrations for
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Database initialization happens in KitabAlHudaApplication
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add listening_history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS listening_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        audioId INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY(audioId) REFERENCES audios(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_listening_history_audioId ON listening_history(audioId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_listening_history_timestamp ON listening_history(timestamp)")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add Messenger message ID column for zero-rated audio streaming
                database.execSQL("ALTER TABLE audios ADD COLUMN fbMessageId TEXT DEFAULT NULL")
                // Add part number column for multi-part surahs
                database.execSQL("ALTER TABLE audios ADD COLUMN partNumber INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
