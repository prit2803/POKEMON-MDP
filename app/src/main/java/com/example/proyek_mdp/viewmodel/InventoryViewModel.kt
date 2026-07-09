package com.example.proyek_mdp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyek_mdp.Data.repository.InventoryRepository
import com.example.proyek_mdp.Data.repository.PostRepository
import com.example.proyek_mdp.UI.Inventory.InventoryItem
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _inventory = MutableLiveData<List<InventoryItem>>()
    val inventory: LiveData<List<InventoryItem>> = _inventory

    fun loadInventory(userId: Int) {

        viewModelScope.launch {

            val inventory =
                inventoryRepository.getUserInventory(userId)

            val list =
                mutableListOf<InventoryItem>()

            for(item in inventory){

                val post =
                    postRepository.getPostById(item.postId)

                if(post!=null){

                    list.add(

                        InventoryItem(

                            postId = post.id,

                            title = post.title,

                            imagePath = post.imagePath,

                            quantity = item.quantity

                        )

                    )

                }

            }

            _inventory.value = list

        }

    }

}