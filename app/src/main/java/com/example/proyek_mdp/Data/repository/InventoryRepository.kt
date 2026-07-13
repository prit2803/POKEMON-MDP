package com.example.proyek_mdp.Data.repository

import android.content.Context
import android.util.Log
import com.example.proyek_mdp.Data.local.datasource.InventoryLocalDataSource
import com.example.proyek_mdp.Data.local.entity.UserInventory
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import com.example.proyek_mdp.Utils.NetworkUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InventoryRepository(
    private val localDataSource: InventoryLocalDataSource,
    private val context: Context
) {
    private val api = BackendRetrofitClient.api

    suspend fun getUserInventory(userId: Int): List<UserInventory> {
        if (NetworkUtils.isOnline(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inventory = api.getUserInventory(userId)
                    for (item in inventory) {
                        val existing = localDataSource.getItem(userId, item.postId)
                        if (existing == null || existing.isSynced == 1) {
                            item.isSynced = 1
                            localDataSource.insert(item)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InventoryRepository", "API getUserInventory failed", e)
                }
            }
        }
        return localDataSource.getUserInventory(userId)
    }

    suspend fun getItem(userId: Int, postId: Int): UserInventory? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val item = api.getItem(userId, postId)
                if (item != null) {
                    val existing = localDataSource.getItem(userId, postId)
                    if (existing == null || existing.isSynced == 1) {
                        item.isSynced = 1
                        localDataSource.insert(item)
                    }
                }
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API getItem failed", e)
            }
        }
        return localDataSource.getItem(userId, postId)
    }

    suspend fun insert(item: UserInventory) {
        if (NetworkUtils.isOnline(context)) {
            try {
                api.syncInventory(item)
                item.isSynced = 1
                localDataSource.insert(item)
                return
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API syncInventory failed", e)
            }
        }
        item.isSynced = 0
        localDataSource.insert(item)
    }

    suspend fun update(item: UserInventory) {
        if (NetworkUtils.isOnline(context)) {
            try {
                api.syncInventory(item)
                item.isSynced = 1
                localDataSource.update(item)
                return
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API syncInventory failed", e)
            }
        }
        item.isSynced = 0
        localDataSource.update(item)
    }
}