package com.example.proyek_mdp.Data.local.database

import android.content.Context
import com.example.proyek_mdp.Data.local.dao.*
import com.example.proyek_mdp.Data.remote.dao.*

class AppDatabase private constructor(context: Context) {

    fun paymentHistoryDao(): PaymentHistoryDao = ApiPaymentHistoryDao()
    fun purchaseHistoryDao(): PurchaseHistoryDao = ApiPurchaseHistoryDao()
    fun userDao(): UserDao = ApiUserDao()
    fun postDao(): PostDao = ApiPostDao()
    fun userInventoryDao(): UserInventoryDao = ApiUserInventoryDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}