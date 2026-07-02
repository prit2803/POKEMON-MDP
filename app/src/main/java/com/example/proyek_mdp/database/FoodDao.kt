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
}