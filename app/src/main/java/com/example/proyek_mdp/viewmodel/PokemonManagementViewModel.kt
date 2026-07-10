package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.repository.PokemonRepository
import kotlinx.coroutines.launch

class PokemonManagementViewModel(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _pokemonList = MutableLiveData<List<PokemonEntity>>()
    val pokemonList: LiveData<List<PokemonEntity>> = _pokemonList

    private val _deleteSuccessMessage = MutableLiveData<String>()
    val deleteSuccessMessage: LiveData<String> = _deleteSuccessMessage

    fun loadPokemon() {
        viewModelScope.launch {
            try {
                _pokemonList.value = pokemonRepository.getAllPokemon()
            } catch (e: Exception) {
                _pokemonList.value = emptyList()
            }
        }
    }

    fun deletePokemon(pokemon: PokemonEntity) {
        viewModelScope.launch {
            pokemonRepository.deletePokemon(pokemon)
            _deleteSuccessMessage.value = "${pokemon.name} dihapus"
            loadPokemon()
        }
    }
}
