package com.example.proyek_mdp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Int,
    val description: String = "",
    val emoji: String = "🍎",
    var stock: Int = 10 // stok global, sama untuk semua user
)