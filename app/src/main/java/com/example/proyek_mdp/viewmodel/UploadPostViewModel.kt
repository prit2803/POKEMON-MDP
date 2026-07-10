package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.repository.PostRepository
import kotlinx.coroutines.launch

class UploadPostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _saveError = MutableLiveData<String>()
    val saveError: LiveData<String> = _saveError

    fun savePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.insertPost(post)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Gagal menyimpan post"
            }
        }
    }
}
