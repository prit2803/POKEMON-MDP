package com.example.proyek_mdp.Data.local.datasource

import com.example.proyek_mdp.Data.local.dao.UserDao
import com.example.proyek_mdp.Data.local.entity.User

class UserLocalDataSource(
    private val userDao: UserDao
) {

    suspend fun login(
        username: String,
        password: String
    ): User? {
        return userDao.login(username, password)
    }

    suspend fun insert(user: User) {
        userDao.insert(user)
    }

    suspend fun update(user: User) {
        userDao.update(user)
    }

    suspend fun delete(user: User) {
        userDao.delete(user)
    }

    suspend fun isUsernameExists(username: String): Int {
        return userDao.isUsernameExists(username)
    }

    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun updateCoin(id: Int, coin: Int) {
        userDao.updateCoin(id, coin)
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun getTotalUsers(): Int {
        return userDao.getTotalUsers()
    }

    suspend fun getBannedUsersCount(): Int {
        return userDao.getBannedUsersCount()
    }

    suspend fun updateBannedStatus(userId: Int, status: Int) {
        userDao.updateBannedStatus(userId, status)
    }
}