package com.example.proyek_mdp.UI.Collection

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokedexAdapter
import com.example.proyek_mdp.UI.Adapter.PokedexItem
import com.example.proyek_mdp.UI.Database.PokedexSpecies
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Network.RetrofitClient
import com.example.proyek_mdp.auth.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Popup Pokedex full-screen. Nampilin SEMUA pokemon 1 generasi (dipilih lewat tombol
 * gen di atas) — yang udah ditangkap user tampil normal + info, yang belum jadi siluet hitam.
 *
 * Data spesies (nama, gambar, tipe) di-cache di Room (pokedex_species_cache) supaya generasi
 * yang udah pernah dibuka gak fetch API lagi tiap kali dialog ini dibuka ulang.
 */
class PokedexDialogFragment : DialogFragment(R.layout.fragment_pokedex_dialog) {

    private lateinit var rvPokedex: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PokedexAdapter

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPokedex = view.findViewById(R.id.rvPokedex)
        progressBar = view.findViewById(R.id.progressBarPokedex)

        rvPokedex.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = PokedexAdapter()
        rvPokedex.adapter = adapter

        val genContainer = view.findViewById<LinearLayout>(R.id.genButtonsContainer)
        GENERATIONS.forEachIndexed { index, gen ->
            val button = Button(requireContext()).apply {
                text = gen.label
                textSize = 12f
                isAllCaps = false
                setPadding(32, 12, 32, 12)
                setOnClickListener { loadGeneration(index) }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = 8
            button.layoutParams = params
            genContainer.addView(button)
        }

        view.findViewById<Button>(R.id.btnClosePokedex).setOnClickListener { dismiss() }

        loadGeneration(0) // default Gen 1
    }

    private fun loadGeneration(index: Int) {
        val gen = GENERATIONS[index]

        progressBar.visibility = View.VISIBLE
        rvPokedex.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val pokemonDb = PokemonDatabase.getDatabase(requireContext())
            val userId = SessionManager(requireContext()).getUserId()

            // 1. Cek cache lokal, fetch dari API cuma yang belum ada
            val cached = pokemonDb.pokedexSpeciesDao().getRange(gen.range.first, gen.range.last)
            val cachedIds = cached.map { it.speciesId }.toSet()
            val missingIds = gen.range.filter { it !in cachedIds }

            if (missingIds.isNotEmpty()) {
                val fetched = coroutineScope {
                    missingIds.map { id ->
                        async(Dispatchers.IO) {
                            try {
                                val response = RetrofitClient.api.getPokemon(id.toString())
                                if (response.isSuccessful) {
                                    response.body()?.let { body ->
                                        PokedexSpecies(
                                            speciesId = body.id,
                                            name = body.name,
                                            imageUrl = body.sprites.front_default,
                                            type1 = body.types.getOrNull(0)?.type?.name ?: "",
                                            type2 = body.types.getOrNull(1)?.type?.name
                                        )
                                    }
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }
                if (fetched.isNotEmpty()) {
                    pokemonDb.pokedexSpeciesDao().insertAll(fetched)
                }
            }

            val allSpecies = pokemonDb.pokedexSpeciesDao().getRange(gen.range.first, gen.range.last)

            // 2. Data pokemon yang dimiliki user (level tertinggi + tanggal pertama tangkap per spesies)
            val ownedSummary = if (userId != -1) {
                pokemonDb.pokemonDao().getOwnedSpeciesSummary(userId).associateBy { it.speciesId }
            } else {
                emptyMap()
            }

            val items = allSpecies.map { species ->
                val owned = ownedSummary[species.speciesId]
                PokedexItem(
                    species = species,
                    isCaught = owned != null,
                    highestLevel = owned?.highestLevel ?: 0,
                    firstCaughtAt = owned?.firstCaughtAt ?: 0L
                )
            }

            if (isAdded) {
                adapter.updateData(items)
                progressBar.visibility = View.GONE
                rvPokedex.visibility = View.VISIBLE
            }
        }
    }

    private data class PokedexGeneration(val label: String, val range: IntRange)

    companion object {
        // Rentang nomor Pokedex nasional per generasi (data statis, bukan dari API)
        private val GENERATIONS = listOf(
            PokedexGeneration("Gen 1", 1..151),
            PokedexGeneration("Gen 2", 152..251),
            PokedexGeneration("Gen 3", 252..386),
            PokedexGeneration("Gen 4", 387..493),
            PokedexGeneration("Gen 5", 494..649),
            PokedexGeneration("Gen 6", 650..721),
            PokedexGeneration("Gen 7", 722..809),
            PokedexGeneration("Gen 8", 810..905),
            PokedexGeneration("Gen 9", 906..1025)
        )
    }
}