package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.repository.PostRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FeedViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _activePosts = MutableLiveData<List<Post>>()
    val activePosts: LiveData<List<Post>> = _activePosts

    private val _filteredPosts = MutableLiveData<List<Post>>()
    val filteredPosts: LiveData<List<Post>> = _filteredPosts

    private var currentFilter: String? = null

    fun loadActivePosts() {
        viewModelScope.launch {
            postRepository.getActivePosts().collect { posts ->
                _activePosts.value = posts
                applyFilter(posts)
            }
        }
    }

    fun filterByCategory(category: String?) {
        currentFilter = category
        _activePosts.value?.let {
            applyFilter(it)
        }
    }

    private fun applyFilter(posts: List<Post>) {
        val filter = currentFilter
        if (filter == null) {
            _filteredPosts.value = posts
        } else {
            _filteredPosts.value = posts.filter { it.category == filter }
        }
    }
}
