package com.example.proyek_mdp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_food")
data class UserFood(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val foodId: Int,
    var quantity: Int = 0
)