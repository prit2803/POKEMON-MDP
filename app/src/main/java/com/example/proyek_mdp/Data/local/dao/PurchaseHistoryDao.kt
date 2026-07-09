package com.example.proyek_mdp.Data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.proyek_mdp.Data.local.entity.PurchaseHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseHistoryDao {

    @Insert
    suspend fun insert(history: PurchaseHistory)

    @Query("""
        SELECT *
        FROM purchase_history
        WHERE userId = :userId
        ORDER BY purchaseDate DESC
    """)
    fun getHistory(userId: Int): Flow<List<PurchaseHistory>>

}