package com.example.proyek_mdp.Data.repository

import com.example.proyek_mdp.Data.local.datasource.PostLocalDataSource
import com.example.proyek_mdp.Data.local.entity.Post
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val localDataSource: PostLocalDataSource
) {

    fun getAllPosts(): Flow<List<Post>> {
        return localDataSource.getAllPosts()
    }

    fun getActivePosts(): Flow<List<Post>> {
        return localDataSource.getActivePosts()
    }

    suspend fun insertPost(post: Post): Long {
        return localDataSource.insertPost(post)
    }

    suspend fun updatePost(post: Post) {
        localDataSource.updatePost(post)
    }

    suspend fun deletePost(post: Post) {
        localDataSource.deletePost(post)
    }

    suspend fun getPostById(postId: Int): Post? {
        return localDataSource.getPostById(postId)
    }

    suspend fun decreaseStock(postId: Int): Int {
        return localDataSource.decreaseStock(postId)
    }

    suspend fun getTotalPosts(): Int {
        return localDataSource.getTotalPosts()
    }
}