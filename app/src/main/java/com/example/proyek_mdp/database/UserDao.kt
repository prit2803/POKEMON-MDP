package com.example.proyek_mdp.database

import androidx.room.*

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND isBanned = 0")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUsers(): Int

    @Query("SELECT COUNT(*) FROM users WHERE isBanned = 1")
    suspend fun getBannedUsersCount(): Int

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE users SET isBanned = :status WHERE id = :userId")
    suspend fun updateBannedStatus(userId: Int, status: Int)
}