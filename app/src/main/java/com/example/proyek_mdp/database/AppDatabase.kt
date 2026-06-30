package com.example.proyek_mdp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class],
    version = 2, // Versi sudah benar 2
    exportSchema = false // Tambahkan ini untuk menghindari peringatan folder skema
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

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
                    .fallbackToDestructiveMigration() // TAMBAHKAN BARIS INI (SANGAT PENTING)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}