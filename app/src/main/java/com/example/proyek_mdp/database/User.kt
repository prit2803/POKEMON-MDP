package com.example.proyek_mdp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var username: String,
    var email: String,
    var password: String,
    var role: String = "user",
    var isBanned: Int = 0,
    var coins: Int = 0,              // saldo koin user
    var lastClaimDate: String? = null, // format "yyyy-MM-dd", null = belum pernah klaim
    var streakCount: Int = 0          // jumlah hari beruntun klaim
)