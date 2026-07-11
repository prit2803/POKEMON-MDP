package com.example.proyek_mdp.Data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyek_mdp.Data.local.dao.*
import com.example.proyek_mdp.Data.local.entity.*

@Database(
    entities = [
        User::class,
        Post::class,
        UserInventory::class,
        PaymentHistory::class,
        PurchaseHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun paymentHistoryDao(): PaymentHistoryDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun userInventoryDao(): UserInventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}