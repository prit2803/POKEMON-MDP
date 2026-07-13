package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_inventory", primaryKeys = ["userId", "postId"])
data class UserInventory(
    val userId: Int,
    val postId: Int,
    var quantity: Int = 0,
    var isSynced: Int = 1
)