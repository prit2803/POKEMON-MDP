package com.example.proyek_mdp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("""
        SELECT * FROM users
        WHERE username = :username
        AND password = :password
        LIMIT 1
    """)
    suspend fun login(
        username: String,
        password: String
    ): User?

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    suspend fun isUsernameExists(username: String): Int
}