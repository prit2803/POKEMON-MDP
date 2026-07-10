package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.Data.repository.UserRepository
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usersList = MutableLiveData<List<User>>()
    val usersList: LiveData<List<User>> = _usersList

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _usersList.value = userRepository.getAllUsers()
            } catch (e: Exception) {
                _usersList.value = emptyList()
            }
        }
    }

    fun toggleBan(user: User) {
        viewModelScope.launch {
            val newStatus = if (user.isBanned == 1) 0 else 1
            userRepository.updateBannedStatus(user.id, newStatus)
            loadUsers()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userRepository.delete(user)
            loadUsers()
        }
    }

    fun updateUsername(user: User, newUsername: String) {
        viewModelScope.launch {
            user.username = newUsername
            userRepository.update(user)
            loadUsers()
        }
    }
}
