package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.repository.PokemonRepository
import com.example.proyek_mdp.Data.repository.UserRepository
import kotlinx.coroutines.launch

class CameraViewModel(
    private val pokemonRepository: PokemonRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _pokemonList = MutableLiveData<List<PokemonEntity>>()
    val pokemonList: LiveData<List<PokemonEntity>> = _pokemonList

    private val _catchSuccess = MutableLiveData<Boolean>()
    val catchSuccess: LiveData<Boolean> = _catchSuccess

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadPokemon(userId: Int) {
        viewModelScope.launch {
            try {
                _pokemonList.value = pokemonRepository.getPokemonByUser(userId)
            } catch (e: Exception) {
                _pokemonList.value = emptyList()
                _error.value = e.message ?: "Gagal memuat daftar Pokemon"
            }
        }
    }

    fun catchPokemon(
        userId: Int,
        speciesId: Int,
        name: String,
        hp: Int,
        imageUrl: String
    ) {
        viewModelScope.launch {
            try {
                // 1. Simpan pokemon yang tertangkap
                val newPokemon = PokemonEntity(
                    userId = userId,
                    speciesId = speciesId,
                    name = name,
                    hp = hp,
                    imageUrl = imageUrl
                )
                pokemonRepository.insertPokemon(newPokemon)

                // 2. Tambah statistik user
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    user.pokemonCaught += 1
                    user.trainerLevel = (user.pokemonCaught / 5) + 1
                    user.distance += 0.1
                    userRepository.update(user)
                }

                _catchSuccess.value = true
            } catch (e: Exception) {
                _catchSuccess.value = false
                _error.value = e.message ?: "Gagal menangkap Pokemon"
            }
        }
    }
}
