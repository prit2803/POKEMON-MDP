package com.example.proyek_mdp.UI.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, PokedexSpecies::class],
    version = 4, // naik dari 3 -> 4: tambah speciesId/caughtAt di PokemonEntity + tabel cache Pokedex baru
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun pokedexSpeciesDao(): PokedexSpeciesDao

    companion object {

        @Volatile
        private var INSTANCE: PokemonDatabase? = null

        fun getDatabase(
            context: Context
        ): PokemonDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        PokemonDatabase::class.java,
                        "pokemon_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}