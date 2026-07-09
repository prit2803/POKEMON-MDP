package com.example.proyek_mdp.Data.local.datasource

import com.example.proyek_mdp.Data.local.dao.PokemonDao
import com.example.proyek_mdp.Data.local.entity.OwnedSpeciesSummary
import com.example.proyek_mdp.Data.local.entity.PokemonEntity

class PokemonLocalDataSource(
    private val pokemonDao: PokemonDao
) {

    suspend fun insertPokemon(pokemon: PokemonEntity) =
        pokemonDao.insertPokemon(pokemon)

    suspend fun updatePokemon(pokemon: PokemonEntity) =
        pokemonDao.updatePokemon(pokemon)

    suspend fun getAllPokemon() =
        pokemonDao.getAllPokemon()

    suspend fun getPokemonByUser(userId: Int) =
        pokemonDao.getPokemonByUser(userId)

    suspend fun getStarter(userId: Int) =
        pokemonDao.getStarter(userId)

    suspend fun deletePokemon(pokemon: PokemonEntity) =
        pokemonDao.deletePokemon(pokemon)

    suspend fun deleteAllUnlockedByUser(userId: Int) =
        pokemonDao.deleteAllUnlockedByUser(userId)

    suspend fun getOwnedSpeciesSummary(userId: Int) =
        pokemonDao.getOwnedSpeciesSummary(userId)
}