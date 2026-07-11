package com.example.proyek_mdp.Data.repository

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.proyek_mdp.Data.local.datasource.PostLocalDataSource
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import com.example.proyek_mdp.Data.worker.SyncWorker
import com.example.proyek_mdp.Utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PostRepository(
    private val localDataSource: PostLocalDataSource,
    private val context: Context
) {
    private val api = BackendRetrofitClient.api
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

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

    private fun refreshAllPosts() {
        repositoryScope.launch {
            if (NetworkUtils.isOnline(context)) {
                try {
                    val posts = api.getAllPosts()
                    for (post in posts) {
                        post.isSynced = 1
                        localDataSource.insertPost(post)
                    }
                } catch (e: Exception) {
                    Log.e("PostRepository", "API getAllPosts failed", e)
                }
            }
        }
    }

    private fun refreshActivePosts() {
        repositoryScope.launch {
            if (NetworkUtils.isOnline(context)) {
                try {
                    val posts = api.getActivePosts()
                    for (post in posts) {
                        post.isSynced = 1
                        localDataSource.insertPost(post)
                    }
                } catch (e: Exception) {
                    Log.e("PostRepository", "API getActivePosts failed", e)
                }
            }
        }
    }

    fun getAllPosts(): Flow<List<Post>> {
        refreshAllPosts()
        return localDataSource.getAllPosts()
    }

    fun getActivePosts(): Flow<List<Post>> {
        refreshActivePosts()
        return localDataSource.getActivePosts()
    }

    suspend fun insertPost(post: Post): Long {
        post.isSynced = 0
        val localId = localDataSource.insertPost(post)
        if (NetworkUtils.isOnline(context)) {
            try {
                val serverId = api.insertPost(post)
                if (serverId > 0) {
                    post.isSynced = 1
                    localDataSource.updatePost(post)
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "API insertPost failed", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
        return localId
    }

    suspend fun updatePost(post: Post) {
        post.isSynced = 0
        localDataSource.updatePost(post)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.updatePost(post)
                post.isSynced = 1
                localDataSource.updatePost(post)
            } catch (e: Exception) {
                Log.e("PostRepository", "API updatePost failed", e)
                scheduleSync()
            }
        } else {
            scheduleSync()
        }
    }

    suspend fun deletePost(post: Post) {
        localDataSource.deletePost(post)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.deletePost(post)
            } catch (e: Exception) {
                Log.e("PostRepository", "API deletePost failed", e)
            }
        }
    }

    suspend fun getPostById(postId: Int): Post? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val post = api.getPostById(postId)
                if (post != null) {
                    post.isSynced = 1
                    localDataSource.insertPost(post)
                    return post
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "API getPostById failed", e)
            }
        }
        return localDataSource.getPostById(postId)
    }

    suspend fun decreaseStock(postId: Int): Int {
        val affected = localDataSource.decreaseStock(postId)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.decreaseStock(postId)
            } catch (e: Exception) {
                Log.e("PostRepository", "API decreaseStock failed", e)
            }
        }
        return affected
    }

    suspend fun getTotalPosts(): Int {
        if (NetworkUtils.isOnline(context)) {
            try {
                return api.getTotalPosts()
            } catch (e: Exception) {
                Log.e("PostRepository", "API getTotalPosts failed", e)
            }
        }
        return localDataSource.getTotalPosts()
    }
}