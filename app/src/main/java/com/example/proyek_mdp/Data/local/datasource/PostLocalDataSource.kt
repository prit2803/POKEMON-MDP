package com.example.proyek_mdp.Data.local.datasource

import com.example.proyek_mdp.Data.local.dao.PostDao
import com.example.proyek_mdp.Data.local.entity.Post
import kotlinx.coroutines.flow.Flow

class PostLocalDataSource(
    private val postDao: PostDao
) {

    fun getAllPosts(): Flow<List<Post>> {
        return postDao.getAllPosts()
    }

    fun getActivePosts(): Flow<List<Post>> {
        return postDao.getActivePosts()
    }

    suspend fun insertPost(post: Post): Long {
        return postDao.insertPost(post)
    }

    suspend fun updatePost(post: Post) {
        postDao.updatePost(post)
    }

    suspend fun deletePost(post: Post) {
        postDao.deletePost(post)
    }

    suspend fun getPostById(postId: Int): Post? {
        return postDao.getPostById(postId)
    }

    suspend fun decreaseStock(postId: Int): Int {
        return postDao.decreaseStock(postId)
    }

    suspend fun getTotalPosts(): Int {
        return postDao.getTotalPosts()
    }
}