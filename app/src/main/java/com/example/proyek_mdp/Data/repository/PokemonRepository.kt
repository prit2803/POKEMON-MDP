package com.example.proyek_mdp.Data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.proyek_mdp.Data.local.datasource.PokemonLocalDataSource
import com.example.proyek_mdp.Data.local.entity.OwnedSpeciesSummary
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import com.example.proyek_mdp.Data.remote.datasource.PokemonRemoteDataSource
import com.example.proyek_mdp.Data.remote.response.PokemonResponse
import com.example.proyek_mdp.Data.worker.SyncWorker
import com.example.proyek_mdp.Utils.NetworkUtils

class PokemonRepository(
    private val localDataSource: PokemonLocalDataSource,
    private val remoteDataSource: PokemonRemoteDataSource,
    private val context: Context
) {
    private val api = BackendRetrofitClient.api

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "OfflineSyncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    suspend fun insertPokemon(pokemon: PokemonEntity): Long {
        pokemon.isSynced = 0
        val localId = localDataSource.insertPokemon(pokemon)
        if (NetworkUtils.isOnline(context)) {
            try {
                // If ID is 0, backend insert will auto generate.
                // We send the pokemon to server:
                val serverId = api.insertPokemon(pokemon)
                if (serverId > 0) {
                    // Update local pokemon with server's generated ID if it was new
                    // Or keep the original local ID but mark it synced.
                    pokemon.isSynced = 1
                    localDataSource.updatePokemon(pokemon)
                }
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API insertPokemon failed", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
        return localId
    }

    suspend fun updatePokemon(pokemon: PokemonEntity) {
        pokemon.isSynced = 0
        localDataSource.updatePokemon(pokemon)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.updatePokemon(pokemon)
                pokemon.isSynced = 1
                localDataSource.updatePokemon(pokemon)
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API updatePokemon failed", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
    }

    suspend fun getAllPokemon(): List<PokemonEntity> {
        if (NetworkUtils.isOnline(context)) {
            try {
                val pokemons = api.getAllPokemon()
                for (pokemon in pokemons) {
                    pokemon.isSynced = 1
                    localDataSource.insertPokemon(pokemon)
                }
                return pokemons
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API getAllPokemon failed", e)
            }
        }
        return localDataSource.getAllPokemon()
    }

    suspend fun getPokemonByUser(userId: Int): List<PokemonEntity> {
        if (NetworkUtils.isOnline(context)) {
            try {
                val pokemons = api.getPokemonByUser(userId)
                for (pokemon in pokemons) {
                    pokemon.isSynced = 1
                    localDataSource.insertPokemon(pokemon)
                }
                return pokemons
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API getPokemonByUser failed", e)
            }
        }
        return localDataSource.getPokemonByUser(userId)
    }

    suspend fun getStarter(userId: Int): PokemonEntity? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val pokemon = api.getStarter(userId)
                if (pokemon != null) {
                    pokemon.isSynced = 1
                    localDataSource.insertPokemon(pokemon)
                    return pokemon
                }
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API getStarter failed", e)
            }
        }
        return localDataSource.getStarter(userId)
    }

    suspend fun deletePokemon(pokemon: PokemonEntity) {
        localDataSource.deletePokemon(pokemon)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.deletePokemon(pokemon)
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API deletePokemon failed", e)
            }
        }
    }

    suspend fun deleteAllUnlockedByUser(userId: Int) {
        localDataSource.deleteAllUnlockedByUser(userId)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.deleteAllUnlockedByUser(userId)
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API deleteAllUnlockedByUser failed", e)
            }
        }
    }

    suspend fun getOwnedSpeciesSummary(userId: Int): List<OwnedSpeciesSummary> {
        if (NetworkUtils.isOnline(context)) {
            try {
                return api.getOwnedSpeciesSummary(userId)
            } catch (e: Exception) {
                Log.e("PokemonRepository", "API getOwnedSpeciesSummary failed", e)
            }
        }
        return localDataSource.getOwnedSpeciesSummary(userId)
    }

    suspend fun fetchPokemon(name: String): PokemonResponse? {
        return remoteDataSource.getPokemon(name)
    }
}