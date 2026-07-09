package com.example.proyek_mdp.Data.remote.api

import com.example.proyek_mdp.Data.remote.response.PokemonResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {

    @GET("pokemon/{name}")
    suspend fun getPokemon(
        @Path("name") name: String
    ): Response<PokemonResponse>
}