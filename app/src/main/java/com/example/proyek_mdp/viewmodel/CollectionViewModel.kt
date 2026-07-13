package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.local.entity.UserInventory
import com.example.proyek_mdp.Data.repository.InventoryRepository
import com.example.proyek_mdp.Data.repository.PokemonRepository
import com.example.proyek_mdp.Data.repository.PostRepository
import kotlinx.coroutines.launch
import kotlin.math.ceil
class CollectionViewModel(
    private val pokemonRepository: PokemonRepository,
    private val inventoryRepository: InventoryRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    // ===============================
    // Pokemon List
    // ===============================

    private val _pokemonList = MutableLiveData<List<PokemonEntity>>()
    val pokemonList: LiveData<List<PokemonEntity>> = _pokemonList
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    // ===============================
    // Load Pokemon
    // ===============================

    fun loadPokemon(userId: Int) {
        viewModelScope.launch {
            _pokemonList.value =
                pokemonRepository.getPokemonByUser(userId)
        }
    }

    // ===============================
    // Lock / Unlock
    // ===============================

    fun toggleLock(pokemon: PokemonEntity) {

        viewModelScope.launch {

            val newLockState =
                if (pokemon.isLocked == 1) 0
                else 1

            pokemonRepository.updatePokemon(
                pokemon.copy(
                    isLocked = newLockState
                )
            )

            loadPokemon(pokemon.userId)
            _toastMessage.value =
                if(newLockState==1)
                    "${pokemon.name} dikunci"
                else
                    "${pokemon.name} dibuka kuncinya"
        }
    }

    // ===============================
    // Delete One
    // ===============================

    fun deletePokemon(
        pokemon: PokemonEntity
    ) {

        viewModelScope.launch {

            pokemonRepository.deletePokemon(
                pokemon
            )

            loadPokemon(
                pokemon.userId
            )
            _toastMessage.value =
                "${pokemon.name} dihapus"
        }
    }

    // ===============================
    // Delete All Unlocked
    // ===============================

    fun deleteAllUnlocked(
        userId: Int
    ) {

        viewModelScope.launch {

            pokemonRepository.deleteAllUnlockedByUser(
                userId
            )

            loadPokemon(
                userId
            )
        }
        _toastMessage.value =
            "Pokemon yang tidak terkunci berhasil dihapus"
    }
    suspend fun getFoodList(userId: Int): List<Pair<UserInventory, Post>> {
        val inventory = inventoryRepository.getUserInventory(userId)
        val foods = mutableListOf<Pair<UserInventory, Post>>()
        for (item in inventory) {
            if (item.quantity <= 0) continue
            val post = postRepository.getPostById(item.postId)
            if (post != null && post.category == "Makanan") {
                foods.add(item to post)
            }
        }
        return foods
    }

    fun feedPokemon(
        pokemon: PokemonEntity,
        inventoryItem: UserInventory,
        food: Post
    ) {

        viewModelScope.launch {

            inventoryItem.quantity--

            inventoryRepository.update(
                inventoryItem
            )

            val expGain =
                calculateExpGain(food.price)

            var exp =
                pokemon.exp + expGain

            var level =
                pokemon.level

            var leveledUp = false

            while (exp >= expThreshold(level)) {

                exp -= expThreshold(level)

                level++

                leveledUp = true

            }

            pokemonRepository.updatePokemon(

                pokemon.copy(

                    exp = exp,

                    level = level

                )

            )

            loadPokemon(
                pokemon.userId
            )

        }

    }
    private fun calculateExpGain(price: Double): Int {

        val bracket =
            ceil(price / 5.0)
                .toInt()
                .coerceAtLeast(1)

        return 3 + (bracket - 1) * 2
    }

    private fun expThreshold(level: Int): Int {

        return level * 20

    }

    // ===============================
    // Refresh
    // ===============================

    fun refresh(
        userId: Int
    ) {
        loadPokemon(userId)
    }
}