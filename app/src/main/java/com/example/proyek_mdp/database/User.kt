package com.example.proyek_mdp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var username: String, // Ubah dari val ke var
    var email: String,    // Ubah dari val ke var
    var password: String, // Ubah dari val ke var
    var role: String = "user",
    var isBanned: Int = 0
)