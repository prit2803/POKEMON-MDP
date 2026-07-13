package com.example.proyek_mdp.Data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.proyek_mdp.Data.local.entity.UserInventory

@Dao
interface UserInventoryDao {

    @Query("""
        SELECT *
        FROM user_inventory
        WHERE userId = :userId
    """)
    suspend fun getUserInventory(userId: Int): List<UserInventory>

    @Query("""
        SELECT *
        FROM user_inventory
        WHERE userId = :userId
        AND postId = :postId
        LIMIT 1
    """)
    suspend fun getItem(
        userId: Int,
        postId: Int
    ): UserInventory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UserInventory)

    @Update
    suspend fun update(item: UserInventory)

    @Query("SELECT * FROM user_inventory WHERE isSynced = 0")
    suspend fun getUnsyncedInventory(): List<UserInventory>
}