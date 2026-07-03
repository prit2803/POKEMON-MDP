package com.example.proyek_mdp.UI.Database

import androidx.room.*

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity): Long

    @Update
    suspend fun updatePokemon(pokemon: PokemonEntity)

    @Query("SELECT * FROM pokemon_table")
    suspend fun getAllPokemon(): List<PokemonEntity>

    // Koleksi milik satu user aja
    @Query("SELECT * FROM pokemon_table WHERE userId = :userId")
    suspend fun getPokemonByUser(userId: Int): List<PokemonEntity>

    @Query("SELECT * FROM pokemon_table WHERE userId = :userId AND isStarter = 1 LIMIT 1")
    suspend fun getStarter(userId: Int): PokemonEntity?

    @Delete
    suspend fun deletePokemon(pokemon: PokemonEntity)
}