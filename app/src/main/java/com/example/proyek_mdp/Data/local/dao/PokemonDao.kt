package com.example.proyek_mdp.Data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.proyek_mdp.Data.local.entity.OwnedSpeciesSummary
import com.example.proyek_mdp.Data.local.entity.PokemonEntity

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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

    @Query("""
        SELECT speciesId, MAX(level) AS highestLevel, MIN(caughtAt) AS firstCaughtAt
        FROM pokemon_table
        WHERE userId = :userId AND speciesId != 0
        GROUP BY speciesId
    """)
    suspend fun getOwnedSpeciesSummary(userId: Int): List<OwnedSpeciesSummary>

    @Query("SELECT * FROM pokemon_table WHERE isSynced = 0")
    suspend fun getUnsyncedPokemon(): List<PokemonEntity>
}