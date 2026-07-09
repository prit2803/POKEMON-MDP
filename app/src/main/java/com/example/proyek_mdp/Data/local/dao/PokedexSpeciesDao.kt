package com.example.proyek_mdp.Data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyek_mdp.Data.local.entity.PokedexSpecies

@Dao
interface PokedexSpeciesDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(species: List<PokedexSpecies>)

    @Query("SELECT * FROM pokedex_species_cache WHERE speciesId BETWEEN :start AND :end ORDER BY speciesId")
    suspend fun getRange(start: Int, end: Int): List<PokedexSpecies>
}