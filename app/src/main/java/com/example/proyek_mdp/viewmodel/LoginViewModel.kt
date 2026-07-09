package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.Data.repository.UserRepository

class LoginViewModel(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun login(
        username: String,
        password: String
    ): User? {
        return repository.login(username, password)
    }
}