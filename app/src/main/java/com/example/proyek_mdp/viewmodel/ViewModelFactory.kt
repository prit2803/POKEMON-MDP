package com.example.proyek_mdp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyek_mdp.Data.local.database.AppDatabase
import com.example.proyek_mdp.Data.local.database.PokemonDatabase
import com.example.proyek_mdp.Data.local.datasource.InventoryLocalDataSource
import com.example.proyek_mdp.Data.local.datasource.PokemonLocalDataSource
import com.example.proyek_mdp.Data.local.datasource.PostLocalDataSource
import com.example.proyek_mdp.Data.local.datasource.UserLocalDataSource
import com.example.proyek_mdp.Data.remote.api.RetrofitClient
import com.example.proyek_mdp.Data.remote.datasource.PokemonRemoteDataSource
import com.example.proyek_mdp.Data.repository.InventoryRepository
import com.example.proyek_mdp.Data.repository.PokemonRepository
import com.example.proyek_mdp.Data.repository.PostRepository
import com.example.proyek_mdp.Data.repository.UserRepository

class ViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val appDb = AppDatabase.getDatabase(context)
        val pokemonDb = PokemonDatabase.getDatabase(context)

        val userRepository = UserRepository(
            UserLocalDataSource(
                appDb.userDao()
            ),
            context
        )

        val pokemonRepository = PokemonRepository(
            PokemonLocalDataSource(
                pokemonDb.pokemonDao()
            ),
            PokemonRemoteDataSource(
                RetrofitClient.api
            ),
            context
        )

        val inventoryRepository = InventoryRepository(
            InventoryLocalDataSource(
                appDb.userInventoryDao()
            ),
            context
        )

        val postRepository = PostRepository(
            PostLocalDataSource(
                appDb.postDao()
            ),
            context
        )


        return when {

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(CollectionViewModel::class.java) -> {
                CollectionViewModel(
                    pokemonRepository,
                    inventoryRepository,
                    postRepository
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {

                HomeViewModel(
                    userRepository,
                    pokemonRepository,
                    postRepository
                ) as T

            }

            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {

                InventoryViewModel(
                    inventoryRepository,
                    postRepository
                ) as T

            }

            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(postRepository) as T
            }

            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(userRepository, postRepository) as T
            }

            modelClass.isAssignableFrom(UserManagementViewModel::class.java) -> {
                UserManagementViewModel(userRepository) as T
            }

            modelClass.isAssignableFrom(PokemonManagementViewModel::class.java) -> {
                PokemonManagementViewModel(pokemonRepository) as T
            }

            modelClass.isAssignableFrom(ManagePostsViewModel::class.java) -> {
                ManagePostsViewModel(postRepository) as T
            }

            modelClass.isAssignableFrom(UploadPostViewModel::class.java) -> {
                UploadPostViewModel(postRepository) as T
            }

            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
                CameraViewModel(pokemonRepository, userRepository) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }
    }
}