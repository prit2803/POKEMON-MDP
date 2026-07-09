package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_inventory")
data class UserInventory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val postId: Int,
    var quantity: Int = 0
)