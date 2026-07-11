package com.example.proyek_mdp.Data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.proyek_mdp.Data.local.datasource.UserLocalDataSource
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import com.example.proyek_mdp.Data.worker.SyncWorker
import com.example.proyek_mdp.Utils.NetworkUtils

class UserRepository(
    private val localDataSource: UserLocalDataSource,
    private val context: Context
) {
    private val api = BackendRetrofitClient.api

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "OfflineSyncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    suspend fun login(username: String, password: String): User? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val user = api.login(mapOf("username" to username, "password" to password))
                if (user != null) {
                    user.isSynced = 1
                    localDataSource.insert(user) // cache locally
                    return user
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "API login failed, falling back to local DB", e)
            }
        }
        return localDataSource.login(username, password)
    }

    suspend fun insert(user: User) {
        user.isSynced = 0
        localDataSource.insert(user)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.insertUser(user)
                user.isSynced = 1
                localDataSource.update(user)
            } catch (e: Exception) {
                Log.e("UserRepository", "API insert failed, scheduled sync", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
    }

    suspend fun update(user: User) {
        user.isSynced = 0
        localDataSource.update(user)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.updateUser(user)
                user.isSynced = 1
                localDataSource.update(user)
            } catch (e: Exception) {
                Log.e("UserRepository", "API update failed, scheduled sync", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
    }

    suspend fun isUsernameExists(username: String): Int {
        if (NetworkUtils.isOnline(context)) {
            try {
                return api.isUsernameExists(username)
            } catch (e: Exception) {
                Log.e("UserRepository", "API check username failed, falling back to local", e)
            }
        }
        return localDataSource.isUsernameExists(username)
    }

    suspend fun getUserById(id: Int): User? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val user = api.getUserById(id)
                if (user != null) {
                    user.isSynced = 1
                    localDataSource.insert(user) // cache update
                    return user
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "API getUserById failed, falling back to local", e)
            }
        }
        return localDataSource.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val user = api.getUserByUsername(username)
                if (user != null) {
                    user.isSynced = 1
                    localDataSource.insert(user)
                    return user
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "API getUserByUsername failed, falling back to local", e)
            }
        }
        return localDataSource.getUserByUsername(username)
    }

    suspend fun delete(user: User) {
        localDataSource.delete(user)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.deleteUser(user.id)
            } catch (e: Exception) {
                Log.e("UserRepository", "API delete failed", e)
            }
        }
    }

    suspend fun getAllUsers(): List<User> {
        if (NetworkUtils.isOnline(context)) {
            try {
                val users = api.getAllUsers()
                for (user in users) {
                    user.isSynced = 1
                    localDataSource.insert(user)
                }
                return users
            } catch (e: Exception) {
                Log.e("UserRepository", "API getAllUsers failed, falling back to local", e)
            }
        }
        return localDataSource.getAllUsers()
    }

    suspend fun getTotalUsers(): Int {
        if (NetworkUtils.isOnline(context)) {
            try {
                return api.getTotalUsers()
            } catch (e: Exception) {
                Log.e("UserRepository", "API getTotalUsers failed, falling back to local", e)
            }
        }
        return localDataSource.getTotalUsers()
    }

    suspend fun getBannedUsersCount(): Int {
        if (NetworkUtils.isOnline(context)) {
            try {
                return api.getBannedUsersCount()
            } catch (e: Exception) {
                Log.e("UserRepository", "API getBannedUsersCount failed, falling back to local", e)
            }
        }
        return localDataSource.getBannedUsersCount()
    }

    suspend fun updateBannedStatus(userId: Int, status: Int) {
        val user = localDataSource.getUserById(userId)
        if (user != null) {
            user.isBanned = status
            user.isSynced = 0
            localDataSource.update(user)
        }
        if (NetworkUtils.isOnline(context)) {
            try {
                api.updateBannedStatus(userId, mapOf("status" to status))
                if (user != null) {
                    user.isSynced = 1
                    localDataSource.update(user)
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "API updateBannedStatus failed", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
    }
}