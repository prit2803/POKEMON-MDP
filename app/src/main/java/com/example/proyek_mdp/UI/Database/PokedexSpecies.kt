package com.example.proyek_mdp.UI.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache lokal data spesies dari PokeAPI (bukan punya user tertentu — shared,
 * dipakai buat nampilin seluruh Pokedex termasuk yang belum ditangkap/shadow).
 */
@Entity(tableName = "pokedex_species_cache")
data class PokedexSpecies(
    @PrimaryKey val speciesId: Int,
    val name: String,
    val imageUrl: String,
    val type1: String,
    val type2: String? = null
)