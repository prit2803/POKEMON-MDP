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

    @Query("SELECT * FROM pokemon_table WHERE userId = :userId")
    suspend fun getPokemonByUser(userId: Int): List<PokemonEntity>

    @Query("SELECT * FROM pokemon_table WHERE userId = :userId AND isStarter = 1 LIMIT 1")
    suspend fun getStarter(userId: Int): PokemonEntity?

    @Delete
    suspend fun deletePokemon(pokemon: PokemonEntity)

    @Query("DELETE FROM pokemon_table WHERE userId = :userId AND isLocked = 0")
    suspend fun deleteAllUnlockedByUser(userId: Int)

    // Buat Pokedex: per spesies yang dimiliki user, ambil level tertinggi & tanggal pertama tangkap
    @Query("""
        SELECT speciesId, MAX(level) AS highestLevel, MIN(caughtAt) AS firstCaughtAt
        FROM pokemon_table
        WHERE userId = :userId AND speciesId != 0
        GROUP BY speciesId
    """)
    suspend fun getOwnedSpeciesSummary(userId: Int): List<OwnedSpeciesSummary>
}