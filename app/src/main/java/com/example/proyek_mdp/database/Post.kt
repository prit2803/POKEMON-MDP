package com.example.proyek_mdp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var description: String,
    var price: Double,
    var category: String,          // "Kartu Pokemon" | "Makanan" | "Lainnya"
    var imagePath: String? = null, // path file lokal (bukan content:// URI)
    var isActive: Int = 1,         // 1 = tampil di feed, 0 = disembunyikan admin
    var createdAt: Long = System.currentTimeMillis()
)