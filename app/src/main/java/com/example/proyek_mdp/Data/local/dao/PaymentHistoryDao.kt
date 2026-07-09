package com.example.proyek_mdp.Data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.proyek_mdp.Data.local.entity.PaymentHistory

@Dao
interface PaymentHistoryDao {

    @Insert
    suspend fun insert(history: PaymentHistory)

    @Query("""
        SELECT *
        FROM payment_history
        WHERE userId = :userId
        ORDER BY transactionDate DESC
    """)
    suspend fun getHistory(userId: Int): List<PaymentHistory>
}