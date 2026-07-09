package com.example.proyek_mdp.Data.local.database

import android.content.Context
import com.example.proyek_mdp.Data.local.dao.*
import com.example.proyek_mdp.Data.remote.dao.*

class PokemonDatabase private constructor(context: Context) {

    fun pokemonDao(): PokemonDao = ApiPokemonDao()
    fun pokedexSpeciesDao(): PokedexSpeciesDao = ApiPokedexSpeciesDao()

    companion object {

        @Volatile
        private var INSTANCE: PokemonDatabase? = null

        fun getDatabase(
            context: Context
        ): PokemonDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = PokemonDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}