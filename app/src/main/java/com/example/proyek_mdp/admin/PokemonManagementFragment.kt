package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonAdapter
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.viewmodel.PokemonManagementViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory

class PokemonManagementFragment : Fragment() {

    private lateinit var rvPokemon: RecyclerView
    private lateinit var adapter: PokemonAdapter

    private val viewModel: PokemonManagementViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pokemon_management, container, false)

        rvPokemon = view.findViewById(R.id.rvPokemon)

        setupRecyclerView()

        viewModel.pokemonList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.deleteSuccessMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

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
        viewModel.loadPokemon()
    }

    private fun deletePokemon(pokemon: PokemonEntity) {
        viewModel.deletePokemon(pokemon)
    }
}