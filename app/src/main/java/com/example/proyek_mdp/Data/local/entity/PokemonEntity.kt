package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_table")
data class PokemonEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int = 0, // pemilik pokemon ini (0 = data lama/belum ada pemilik)

    val speciesId: Int = 0, // nomor Pokedex nasional dari API (0 = belum ke-mapping)

    val name: String,

    val hp: Int,

    val imageUrl: String,

    var level: Int = 1,

    var exp: Int = 0,

    val isStarter: Int = 0,

    var isLocked: Int = 0,

    val caughtAt: Long = System.currentTimeMillis(), // buat ditampilin di Pokedex

    var isSynced: Int = 1
)

/** Hasil agregat: per spesies, level tertinggi & tanggal pertama kali ditangkap milik 1 user. */
data class OwnedSpeciesSummary(
    val speciesId: Int,
    val highestLevel: Int,
    val firstCaughtAt: Long
)