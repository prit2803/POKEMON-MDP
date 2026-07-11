package com.example.proyek_mdp.Data.local.dao

import androidx.room.*
import com.example.proyek_mdp.Data.local.entity.Post
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

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Int): Post?

    // Kurangi stok 1 hanya kalau stok masih > 0. Return jumlah baris ter-update:
    // 1 = berhasil, 0 = stok memang sudah habis (dicek atomik di level SQL)
    @Query("UPDATE posts SET stock = stock - 1 WHERE id = :postId AND stock > 0")
    suspend fun decreaseStock(postId: Int): Int

    @Query("SELECT * FROM posts WHERE isSynced = 0")
    suspend fun getUnsyncedPosts(): List<Post>
}