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
    val emoji: String = "🍎" // dipakai sebagai icon sederhana, tanpa perlu asset gambar
)