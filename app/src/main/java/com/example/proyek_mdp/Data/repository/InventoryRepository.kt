package com.example.proyek_mdp.Data.repository

import com.example.proyek_mdp.Data.local.datasource.InventoryLocalDataSource
import com.example.proyek_mdp.Data.local.entity.UserInventory

class InventoryRepository(
    private val localDataSource: InventoryLocalDataSource
) {

    suspend fun getUserInventory(
        userId: Int
    ): List<UserInventory> {
        return localDataSource.getUserInventory(userId)
    }

    suspend fun getItem(
        userId: Int,
        postId: Int
    ): UserInventory? {
        return localDataSource.getItem(userId, postId)
    }

    suspend fun insert(item: UserInventory) {
        localDataSource.insert(item)
    }

    suspend fun update(item: UserInventory) {
        localDataSource.update(item)
    }
}