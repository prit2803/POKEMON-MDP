package com.example.proyek_mdp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "com/example/proyek_mdp/User", // "user" atau "admin"
    val isBanned: Int = 0      // 0 = Aktif, 1 = Banned
)