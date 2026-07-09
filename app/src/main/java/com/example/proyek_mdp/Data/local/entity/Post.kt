package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var description: String,
    var price: Double,
    var category: String,
    var imagePath: String? = null,
    var isActive: Int = 1,
    var stock: Int = 0,
    var createdAt: Long = System.currentTimeMillis()
)