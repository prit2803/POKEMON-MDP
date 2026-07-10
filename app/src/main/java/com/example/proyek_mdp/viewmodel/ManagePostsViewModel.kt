package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.repository.PostRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ManagePostsViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _postsList = MutableLiveData<List<Post>>()
    val postsList: LiveData<List<Post>> = _postsList

    fun loadPosts() {
        viewModelScope.launch {
            postRepository.getAllPosts().collect { posts ->
                _postsList.value = posts
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            postRepository.deletePost(post)
            // loadPosts() is handled automatically because postRepository.getAllPosts()
            // returns a Flow that emits new updates if using Room. With our mock network Flow,
            // we manually refresh by calling loadPosts() or the flow emits once.
            // Since it emits once, manually loading again is cleaner!
            loadPosts()
        }
    }
}
