package com.example.proyek_mdp.Data.local.entity

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
    var hasSelectedStarter: Int = 0,

    // ===== Statistik (dibiarkan apa adanya dulu, belum diutak-atik) =====
    var pokemonCaught: Int = 0,
    var battleWon: Int = 0,
    var trainerLevel: Int = 1,
    var distance: Double = 0.0,

    // ===== Profile settings =====
    var nickname: String? = null, // null = belum di-set, pakai username
    var team: String? = null      // "Mystic" | "Valor" | "Instinct", null = belum pilih
)