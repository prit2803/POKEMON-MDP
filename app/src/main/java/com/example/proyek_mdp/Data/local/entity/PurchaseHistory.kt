package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_history")
data class PurchaseHistory(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    val postId: Int,

    val itemName: String,

    val price: Int,

    val quantity: Int,

    val purchaseDate: Long = System.currentTimeMillis()

)