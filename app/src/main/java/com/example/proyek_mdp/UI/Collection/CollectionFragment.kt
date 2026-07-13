package com.example.proyek_mdp.UI.Collection

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonAdapter

import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.auth.SessionManager

import com.example.proyek_mdp.viewmodel.CollectionViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.ceil

class CollectionFragment
    : Fragment(R.layout.fragment_collection) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PokemonAdapter
    private val viewModel: CollectionViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        recyclerView =
            view.findViewById(R.id.recyclerViewPokemon)

        recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2)

        view.findViewById<TextView>(R.id.btnDeleteAll).setOnClickListener {
            confirmDeleteAll()
        }

        view.findViewById<TextView>(R.id.btnOpenPokedex).setOnClickListener {
            PokedexDialogFragment().show(childFragmentManager, "pokedex")
        }

        loadPokemon()
        observePokemon()
        observeMessage()
    }

    private fun confirmDeleteAll() {

        val userId = sessionManager.getUserId()

        if (userId == -1) return

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Semua Pokemon")
            .setMessage("Semua pokemon yang TIDAK terkunci akan dihapus. Pokemon yang di-lock akan tetap aman. Lanjutkan?")
            .setPositiveButton("Hapus Semua") { _, _ ->

                viewModel.deleteAllUnlocked(userId)

            }
            .setNegativeButton("Batal", null)
            .show()
    }



    private fun confirmDeleteSingle(
        pokemon: PokemonEntity
    ) {

        if (pokemon.isLocked == 1) {



            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pokemon")
            .setMessage("Hapus ${pokemon.name}?")
            .setPositiveButton("Hapus") { _, _ ->

                viewModel.deletePokemon(pokemon)

                Toast.makeText(
                    requireContext(),
                    "${pokemon.name} dihapus",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadPokemon() {

        val userId = sessionManager.getUserId()

        if (userId == -1) return

        viewModel.loadPokemon(userId)
    }

    /** Menu tap-tahan: Lock/Unlock, dan Hapus (cuma muncul kalau gak lagi di-lock). */
    private fun showActionMenu(pokemon: PokemonEntity) {
        val locked = pokemon.isLocked == 1

        val options = if (locked) {
            arrayOf("🔓 Buka Kunci")
        } else {
            arrayOf("🔒 Kunci", "🗑️ Hapus")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(pokemon.name)
            .setItems(options) { _, index ->
                if (locked) {

                    viewModel.toggleLock(pokemon)


                } else {

                    when (index) {

                        0 -> {

                            viewModel.toggleLock(pokemon)
                        }

                        1 -> confirmDeleteSingle(pokemon)

                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /** Tampilkan daftar makanan yang dipunya user (dibeli dari Shop) buat dipilih. */
    private fun showFeedDialog(
        pokemon: PokemonEntity
    ) {

        val userId =
            sessionManager.getUserId()

        if (userId == -1) return

        lifecycleScope.launch {
            val foods = viewModel.getFoodList(userId)
            if (foods.isEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada makanan. Silakan beli di Toko.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val labels = foods.map {
                "${it.second.title} (x${it.first.quantity})"
            }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Beri makan ${pokemon.name}")
                .setItems(labels) { _, index ->
                    val (inventory, post) = foods[index]
                    viewModel.feedPokemon(
                        pokemon,
                        inventory,
                        post
                    )
                }
                .setNegativeButton("Batal", null)
                .show()
        }

    }
    private fun observePokemon() {

        viewModel.pokemonList.observe(viewLifecycleOwner) { pokemonList ->

            if (::adapter.isInitialized) {

                adapter.updateData(pokemonList)

            } else {

                adapter = PokemonAdapter(
                    pokemonList,
                    onItemClick = { pokemon ->
                        showFeedDialog(pokemon)
                    },
                    onItemLongClick = { pokemon ->
                        showActionMenu(pokemon)
                    }
                )

                recyclerView.adapter = adapter

            }

        }

    }
    private fun observeMessage(){

        viewModel.toastMessage.observe(viewLifecycleOwner){

            Toast.makeText(
                requireContext(),
                it,
                Toast.LENGTH_SHORT
            ).show()

        }

    }


}