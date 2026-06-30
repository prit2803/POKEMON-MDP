package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Database.PokemonEntity
import kotlinx.coroutines.launch

class PokemonManagementFragment : Fragment() {

    private lateinit var rvPokemon: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private lateinit var db: PokemonDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pokemon_management, container, false)

        rvPokemon = view.findViewById(R.id.rvPokemon)
        db = PokemonDatabase.getDatabase(requireContext())

        setupRecyclerView()
        loadPokemonData()

        return view
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan callback hapus
        adapter = PokemonAdapter(emptyList()) { pokemon ->
            deletePokemon(pokemon)
        }
        rvPokemon.layoutManager = LinearLayoutManager(requireContext())
        rvPokemon.adapter = adapter
    }

    private fun loadPokemonData() {
        lifecycleScope.launch {
            val list = db.pokemonDao().getAllPokemon()
            adapter.updateData(list)
        }
    }

    private fun deletePokemon(pokemon: PokemonEntity) {
        lifecycleScope.launch {
            db.pokemonDao().deletePokemon(pokemon)
            Toast.makeText(requireContext(), "${pokemon.name} dihapus", Toast.LENGTH_SHORT).show()
            loadPokemonData() // Refresh list
        }
    }
}