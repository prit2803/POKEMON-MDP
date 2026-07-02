package com.example.proyek_mdp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Post::class, Food::class, UserFood::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun foodDao(): FoodDao
    abstract fun userFoodDao(): UserFoodDao

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
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed katalog makanan default saat database pertama kali dibuat
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context.applicationContext).foodDao().insertAll(
                                    listOf(
                                        Food(name = "Pokeblock Merah", price = 20, description = "Makanan dasar untuk semua Pokemon", emoji = "🍎", stock = 20),
                                        Food(name = "Berry Oran", price = 35, description = "Memulihkan sedikit energi Pokemon", emoji = "🍓", stock = 15),
                                        Food(name = "Poffin Spesial", price = 60, description = "Makanan premium kesukaan banyak Pokemon", emoji = "🍰", stock = 10),
                                        Food(name = "Berry Sitrus", price = 50, description = "Menambah semangat Pokemon", emoji = "🍋", stock = 12)
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}