package com.example.proyek_mdp.Data.local.datasource

import com.example.proyek_mdp.Data.local.dao.UserInventoryDao
import com.example.proyek_mdp.Data.local.entity.UserInventory

class InventoryLocalDataSource(
    private val inventoryDao: UserInventoryDao
) {

    suspend fun getUserInventory(userId: Int): List<UserInventory> {
        return inventoryDao.getUserInventory(userId)
    }

    suspend fun getItem(
        userId: Int,
        postId: Int
    ): UserInventory? {
        return inventoryDao.getItem(userId, postId)
    }

    suspend fun insert(item: UserInventory) {
        inventoryDao.insert(item)
    }

    suspend fun update(item: UserInventory) {
        inventoryDao.update(item)
    }
}