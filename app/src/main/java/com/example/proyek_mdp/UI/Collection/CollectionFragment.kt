package com.example.proyek_mdp.UI.Collection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import kotlinx.coroutines.launch

class CollectionFragment
    : Fragment(R.layout.fragment_collection) {

    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        recyclerView =
            view.findViewById(R.id.recyclerViewPokemon)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        loadPokemon()
    }

    private fun loadPokemon() {

        lifecycleScope.launch {

            val database =
                PokemonDatabase.getDatabase(
                    requireContext()
                )

            val pokemonList =
                database.pokemonDao().getAllPokemon()

            recyclerView.adapter = PokemonAdapter(pokemonList) { pokemon ->
                // Untuk User biasa, kita bisa mengosongkan fungsi hapus ini
                // atau biarkan kosong seperti ini: { }
            }
        }
    }
}