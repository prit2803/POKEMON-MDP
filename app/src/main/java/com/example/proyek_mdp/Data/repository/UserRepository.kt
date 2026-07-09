package com.example.proyek_mdp.Data.repository

import com.example.proyek_mdp.Data.local.datasource.UserLocalDataSource
import com.example.proyek_mdp.Data.local.entity.User

class UserRepository(
    private val localDataSource: UserLocalDataSource
) {

    suspend fun login(
        username: String,
        password: String
    ): User? {
        return localDataSource.login(username, password)
    }

    suspend fun insert(user: User) {
        localDataSource.insert(user)
    }

    suspend fun update(user: User) {
        localDataSource.update(user)
    }

    suspend fun isUsernameExists(username: String): Int {
        return localDataSource.isUsernameExists(username)
    }

    suspend fun getUserById(id: Int): User? {
        return localDataSource.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? {
        return localDataSource.getUserByUsername(username)
    }

    suspend fun delete(user: User) {
        localDataSource.delete(user)
    }

    suspend fun getAllUsers(): List<User> {
        return localDataSource.getAllUsers()
    }

    suspend fun getTotalUsers(): Int {
        return localDataSource.getTotalUsers()
    }

    suspend fun getBannedUsersCount(): Int {
        return localDataSource.getBannedUsersCount()
    }

    suspend fun updateBannedStatus(userId: Int, status: Int) {
        localDataSource.updateBannedStatus(userId, status)
    }
}