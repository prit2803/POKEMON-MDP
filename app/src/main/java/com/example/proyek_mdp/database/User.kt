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
    var coins: Int = 0,
    var lastClaimDate: String? = null,
    var streakCount: Int = 0,
    var hasSelectedStarter: Int = 0 // 0 = belum pilih starter, 1 = sudah
)