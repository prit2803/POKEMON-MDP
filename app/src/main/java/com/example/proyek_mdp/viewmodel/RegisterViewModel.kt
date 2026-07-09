package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.Data.repository.UserRepository

class RegisterViewModel(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun isUsernameExists(
        username: String
    ): Int {
        return repository.isUsernameExists(username)
    }

    suspend fun register(
        user: User
    ) {
        repository.insert(user)
    }
}