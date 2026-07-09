package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyek_mdp.Data.repository.PokemonRepository
import com.example.proyek_mdp.Data.repository.PostRepository
import com.example.proyek_mdp.Data.repository.UserRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.UI.Adapter.BannerDisplayItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeViewModel(
    private val userRepository: UserRepository,
    private val pokemonRepository: PokemonRepository,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _coins = MutableLiveData<String>()
    val coins: LiveData<String> = _coins

    private val _starter = MutableLiveData<PokemonEntity?>()
    val starter: LiveData<PokemonEntity?> = _starter

    private val _feed = MutableLiveData<List<Post>>()
    val feed: LiveData<List<Post>> = _feed

    private val _banner = MutableLiveData<List<BannerDisplayItem>>()
    val banner: LiveData<List<BannerDisplayItem>> = _banner
    private val _filteredFeed = MutableLiveData<List<Post>>()
    val filteredFeed: LiveData<List<Post>> = _filteredFeed

    private var allPosts: List<Post> = emptyList()

    private var currentCategory: String? = null

    fun loadCoins(userId: Int) {

        viewModelScope.launch {

            val user = userRepository.getUserById(userId)

            if (user != null) {

                _coins.value =
                    NumberFormat
                        .getNumberInstance(Locale("in", "ID"))
                        .format(user.coins)

            }

        }

    }
    fun loadStarter(userId: Int) {

        viewModelScope.launch {

            _starter.value =
                pokemonRepository.getStarter(userId)

        }

    }

    fun loadFeed() {

        viewModelScope.launch {

            postRepository
                .getActivePosts()
                .collectLatest { posts ->

                    allPosts = posts

                    _feed.value = posts

                    applyFilter(currentCategory)

                    val postBanner =
                        posts.take(8).map {

                            BannerDisplayItem(

                                imageUrl = it.imagePath ?: "",

                                title = it.title,

                                subtitle =
                                    NumberFormat
                                        .getCurrencyInstance(Locale("in", "ID"))
                                        .format(it.price),

                                postId = it.id

                            )

                        }

                    _banner.value =
                        FEATURED_BANNERS + postBanner

                }

        }

    }
    fun applyFilter(
        category: String?
    ) {

        currentCategory = category

        val filtered =

            if (category == null) {

                allPosts

            } else {

                allPosts.filter {

                    it.category == category

                }

            }

        _filteredFeed.value = filtered

    }
    companion object {

        private val FEATURED_BANNERS = listOf(

            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
                title = "Pikachu",
                subtitle = "Maskot Pokemon paling ikonik"
            ),

            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png",
                title = "Charizard",
                subtitle = "Evolusi akhir Charmander"
            ),

            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/150.png",
                title = "Mewtwo",
                subtitle = "Pokemon legendaris hasil rekayasa genetika"
            ),

            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/133.png",
                title = "Eevee",
                subtitle = "Punya banyak jalur evolusi berbeda"
            )

        )

    }
}
