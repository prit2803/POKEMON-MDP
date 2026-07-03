package com.example.proyek_mdp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserInventoryDao {

    @Query("SELECT * FROM user_inventory WHERE userId = :userId")
    suspend fun getUserInventory(userId: Int): List<UserInventory>

    @Query("SELECT * FROM user_inventory WHERE userId = :userId AND postId = :postId LIMIT 1")
    suspend fun getItem(userId: Int, postId: Int): UserInventory?

    @Insert
    suspend fun insert(item: UserInventory)

    @Update
    suspend fun update(item: UserInventory)
}