package com.example.proyek_mdp.Data.repository

import com.example.proyek_mdp.Data.local.datasource.PokemonLocalDataSource
import com.example.proyek_mdp.Data.local.entity.OwnedSpeciesSummary
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.remote.datasource.PokemonRemoteDataSource
import com.example.proyek_mdp.Data.remote.response.PokemonResponse

class PokemonRepository(
    private val localDataSource: PokemonLocalDataSource,
    private val remoteDataSource: PokemonRemoteDataSource
) {

    // ===========================
    // LOCAL
    // ===========================

    suspend fun insertPokemon(
        pokemon: PokemonEntity
    ): Long {
        return localDataSource.insertPokemon(pokemon)
    }

    suspend fun updatePokemon(
        pokemon: PokemonEntity
    ) {
        localDataSource.updatePokemon(pokemon)
    }

    suspend fun getAllPokemon(): List<PokemonEntity> {
        return localDataSource.getAllPokemon()
    }

    suspend fun getPokemonByUser(
        userId: Int
    ): List<PokemonEntity> {
        return localDataSource.getPokemonByUser(userId)
    }

    suspend fun getStarter(
        userId: Int
    ): PokemonEntity? {
        return localDataSource.getStarter(userId)
    }

    suspend fun deletePokemon(
        pokemon: PokemonEntity
    ) {
        localDataSource.deletePokemon(pokemon)
    }

    suspend fun deleteAllUnlockedByUser(
        userId: Int
    ) {
        localDataSource.deleteAllUnlockedByUser(userId)
    }

    suspend fun getOwnedSpeciesSummary(
        userId: Int
    ): List<OwnedSpeciesSummary> {
        return localDataSource.getOwnedSpeciesSummary(userId)
    }

    // ===========================
    // REMOTE
    // ===========================

    suspend fun fetchPokemon(
        name: String
    ): PokemonResponse? {
        return remoteDataSource.getPokemon(name)
    }

}