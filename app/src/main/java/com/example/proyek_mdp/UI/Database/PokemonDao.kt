package com.example.proyek_mdp.UI.Database

import androidx.room.*

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity)

    @Query("SELECT * FROM pokemon_table")
    suspend fun getAllPokemon(): List<PokemonEntity>

    // TAMBAHKAN INI: Fungsi untuk hapus data
    @Delete
    suspend fun deletePokemon(pokemon: PokemonEntity)
}