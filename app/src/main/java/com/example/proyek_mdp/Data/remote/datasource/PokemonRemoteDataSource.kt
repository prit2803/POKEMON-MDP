package com.example.proyek_mdp.Data.remote.datasource

import com.example.proyek_mdp.Data.remote.api.PokeApiService
import com.example.proyek_mdp.Data.remote.response.PokemonResponse

class PokemonRemoteDataSource(
    private val apiService: PokeApiService
) {

    suspend fun getPokemon(name: String): PokemonResponse? {

        val response = apiService.getPokemon(name)

        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}