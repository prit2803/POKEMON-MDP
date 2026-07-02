package com.example.proyek_mdp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(food: Food)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(foods: List<Food>)

    @Query("SELECT * FROM foods")
    suspend fun getAllFood(): List<Food>

    @Query("SELECT * FROM foods WHERE id = :foodId LIMIT 1")
    suspend fun getFoodById(foodId: Int): Food?

    // Kurangi stok 1 hanya kalau stok masih > 0. Return jumlah baris yang ke-update:
    // 1 = berhasil, 0 = stok memang sudah habis (dicek atomik di level SQL, aman dari race condition sederhana)
    @Query("UPDATE foods SET stock = stock - 1 WHERE id = :foodId AND stock > 0")
    suspend fun decreaseStock(foodId: Int): Int
}