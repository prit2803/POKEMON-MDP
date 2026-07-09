package com.example.proyek_mdp.Data.provider

import android.content.Context
import com.example.proyek_mdp.Data.local.database.PokemonDatabase
import com.example.proyek_mdp.Data.local.datasource.PokemonLocalDataSource
import com.example.proyek_mdp.Data.remote.api.RetrofitClient
import com.example.proyek_mdp.Data.remote.datasource.PokemonRemoteDataSource
import com.example.proyek_mdp.Data.repository.PokemonRepository

object RepositoryProvider {

    fun providePokemonRepository(
        context: Context
    ): PokemonRepository {

        val database = PokemonDatabase.getDatabase(context)

        val dao = database.pokemonDao()

        val local = PokemonLocalDataSource(dao)

        val remote = PokemonRemoteDataSource(
            RetrofitClient.api
        )

        return PokemonRepository(
            local,
            remote
        )

    }

}