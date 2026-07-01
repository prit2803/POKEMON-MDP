package com.example.proyek_mdp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Insert
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActivePosts(): Flow<List<Post>>

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getTotalPosts(): Int
}