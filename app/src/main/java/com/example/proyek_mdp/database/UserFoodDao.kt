package com.example.proyek_mdp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserFoodDao {

    @Query("SELECT * FROM user_food WHERE userId = :userId")
    suspend fun getUserFood(userId: Int): List<UserFood>

    @Query("SELECT * FROM user_food WHERE userId = :userId AND foodId = :foodId LIMIT 1")
    suspend fun getUserFoodItem(userId: Int, foodId: Int): UserFood?

    @Insert
    suspend fun insert(userFood: UserFood)

    @Update
    suspend fun update(userFood: UserFood)
}