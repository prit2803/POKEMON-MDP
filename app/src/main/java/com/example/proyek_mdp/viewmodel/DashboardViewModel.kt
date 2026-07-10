package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.repository.PostRepository
import com.example.proyek_mdp.Data.repository.UserRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _totalUsers = MutableLiveData<Int>()
    val totalUsers: LiveData<Int> = _totalUsers

    private val _bannedUsers = MutableLiveData<Int>()
    val bannedUsers: LiveData<Int> = _bannedUsers

    private val _totalPosts = MutableLiveData<Int>()
    val totalPosts: LiveData<Int> = _totalPosts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                _totalUsers.value = userRepository.getTotalUsers()
                _bannedUsers.value = userRepository.getBannedUsersCount()
                _totalPosts.value = postRepository.getTotalPosts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Gagal memuat statistik"
            }
        }
    }
}
