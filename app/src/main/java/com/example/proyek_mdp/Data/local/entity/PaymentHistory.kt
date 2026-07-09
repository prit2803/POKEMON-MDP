package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistory(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    val paymentMethod: String,

    val coinAmount: Int,

    val totalPrice: Int,

    val status: String,

    val transactionDate: Long

)