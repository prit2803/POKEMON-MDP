package com.example.proyek_mdp.UI.Collection

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionFragment : Fragment(R.layout.fragment_collection) {

    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewPokemon)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadPokemon()
    }

    private fun loadPokemon() {

        lifecycleScope.launch {

            val database = PokemonDatabase.getDatabase(requireContext())

            val pokemonList = withContext(Dispatchers.IO) {
                database.pokemonDao().getAllPokemon()
            }

            recyclerView.adapter = PokemonAdapter(pokemonList) { pokemon ->

                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Pokémon")
                    .setMessage("Yakin ingin menghapus ${pokemon.name}?")
                    .setPositiveButton("Hapus") { _, _ ->

                        lifecycleScope.launch {

                            withContext(Dispatchers.IO) {
                                database.pokemonDao().deletePokemon(pokemon)
                            }

                            Toast.makeText(
                                requireContext(),
                                "${pokemon.name} berhasil dihapus",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadPokemon()
                        }

                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
    }
}