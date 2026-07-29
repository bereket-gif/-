package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MezmurEntity::class, SearchHistoryEntity::class], version = 2, exportSchema = false)
abstract class MezmurDatabase : RoomDatabase() {
    abstract fun mezmurDao(): MezmurDao

    companion object {
        @Volatile
        private var INSTANCE: MezmurDatabase? = null

        fun getDatabase(context: Context): MezmurDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MezmurDatabase::class.java,
                    "mezmur_cache_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
