package com.example.proyek_mdp.UI.Inventory

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.InventoryAdapter
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import kotlinx.coroutines.launch

class InventoryFragment : Fragment(R.layout.fragment_inventory) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var tvEmptyInventory: TextView
    private lateinit var adapter: InventoryAdapter
    private lateinit var sessionManager: SessionManager

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        recyclerView = view.findViewById(R.id.rvInventory)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        adapter = InventoryAdapter(emptyList())

        recyclerView.adapter = adapter

        btnBack = view.findViewById(R.id.btnBack)

        tvEmptyInventory = view.findViewById(R.id.tvEmptyInventory)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        loadInventory()
    }

    private fun loadInventory() {

        val userId = sessionManager.getUserId()

        if (userId == -1) return

        lifecycleScope.launch {

            val db =
                AppDatabase.getDatabase(requireContext())

            val inventory =
                db.userInventoryDao()
                    .getUserInventory(userId)

            val inventoryItems =
                mutableListOf<InventoryItem>()

            for (item in inventory) {

                val post =
                    db.postDao()
                        .getPostById(item.postId)

                if (post != null) {

                    inventoryItems.add(

                        InventoryItem(

                            postId = post.id,

                            title = post.title,

                            imagePath = post.imagePath,

                            quantity = item.quantity
                        )
                    )
                }
            }

            if (isAdded) {

                if (inventoryItems.isEmpty()) {

                    tvEmptyInventory.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE

                } else {

                    tvEmptyInventory.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    adapter.updateData(inventoryItems)
                }

            }
        }
    }
}