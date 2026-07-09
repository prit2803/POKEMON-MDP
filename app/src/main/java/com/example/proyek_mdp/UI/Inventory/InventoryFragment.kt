package com.example.proyek_mdp.UI.Inventory

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.InventoryAdapter
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.Data.local.database.AppDatabase
import com.example.proyek_mdp.viewmodel.InventoryViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class InventoryFragment : Fragment(R.layout.fragment_inventory) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var tvEmptyInventory: TextView
    private lateinit var adapter: InventoryAdapter
    private lateinit var sessionManager: SessionManager
    private val viewModel: InventoryViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

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

        val userId = sessionManager.getUserId()

        if(userId!=-1){

            viewModel.loadInventory(userId)

        }

        observeInventory()
    }


    private fun observeInventory(){

        viewModel.inventory.observe(viewLifecycleOwner){ list ->

            if(list.isEmpty()){

                tvEmptyInventory.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

            }else{

                tvEmptyInventory.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                adapter.updateData(list)

            }

        }

    }
}