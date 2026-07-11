package com.example.proyek_mdp.Data.repository

import android.content.Context
import android.util.Log
import com.example.proyek_mdp.Data.local.datasource.InventoryLocalDataSource
import com.example.proyek_mdp.Data.local.entity.UserInventory
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import com.example.proyek_mdp.Utils.NetworkUtils

class InventoryRepository(
    private val localDataSource: InventoryLocalDataSource,
    private val context: Context
) {
    private val api = BackendRetrofitClient.api

    suspend fun getUserInventory(userId: Int): List<UserInventory> {
        if (NetworkUtils.isOnline(context)) {
            try {
                val inventory = api.getUserInventory(userId)
                for (item in inventory) {
                    localDataSource.insert(item)
                }
                return inventory
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API getUserInventory failed", e)
            }
        }
        return localDataSource.getUserInventory(userId)
    }

    suspend fun getItem(userId: Int, postId: Int): UserInventory? {
        if (NetworkUtils.isOnline(context)) {
            try {
                val item = api.getItem(userId, postId)
                if (item != null) {
                    localDataSource.insert(item)
                    return item
                }
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API getItem failed", e)
            }
        }
        return localDataSource.getItem(userId, postId)
    }

    suspend fun insert(item: UserInventory) {
        localDataSource.insert(item)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.insertInventory(item)
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API insertInventory failed", e)
            }
        }
    }

    suspend fun update(item: UserInventory) {
        localDataSource.update(item)
        if (NetworkUtils.isOnline(context)) {
            try {
                api.updateInventory(item)
            } catch (e: Exception) {
                Log.e("InventoryRepository", "API updateInventory failed", e)
            }
        }
    }
}