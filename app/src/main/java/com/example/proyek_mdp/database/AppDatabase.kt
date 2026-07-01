package com.example.proyek_mdp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Post::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokemon_db"
                )
                    .fallbackToDestructiveMigration() // SANGAT PENTING agar tidak crash saat versi naik
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}