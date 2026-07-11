package com.example.proyek_mdp.Data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyek_mdp.Data.local.database.AppDatabase
import com.example.proyek_mdp.Data.local.database.PokemonDatabase
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("SyncWorker", "Starting synchronization background task...")
        try {
            val appDb = AppDatabase.getDatabase(applicationContext)
            val pokemonDb = PokemonDatabase.getDatabase(applicationContext)
            val api = BackendRetrofitClient.api

            // 1. Sync Users
            val unsyncedUsers = appDb.userDao().getUnsyncedUsers()
            for (user in unsyncedUsers) {
                try {
                    api.updateUser(user)
                    user.isSynced = 1
                    appDb.userDao().update(user)
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync user: ${user.username}", e)
                }
            }

            // 2. Sync Posts
            val unsyncedPosts = appDb.postDao().getUnsyncedPosts()
            for (post in unsyncedPosts) {
                try {
                    api.insertPost(post)
                    post.isSynced = 1
                    appDb.postDao().updatePost(post)
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync post: ${post.title}", e)
                }
            }

            // 3. Sync Pokemon
            val unsyncedPokemon = pokemonDb.pokemonDao().getUnsyncedPokemon()
            for (pokemon in unsyncedPokemon) {
                try {
                    api.insertPokemon(pokemon)
                    pokemon.isSynced = 1
                    pokemonDb.pokemonDao().updatePokemon(pokemon)
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Failed to sync pokemon: ${pokemon.name}", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed, will retry", e)
            Result.retry()
        }
    }
}
