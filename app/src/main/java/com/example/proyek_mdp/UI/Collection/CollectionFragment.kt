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
import com.example.proyek_mdp.UI.Database.PokemonEntity
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.Post
import com.example.proyek_mdp.database.UserInventory
import kotlinx.coroutines.launch

class CollectionFragment
    : Fragment(R.layout.fragment_collection) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PokemonAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        recyclerView =
            view.findViewById(R.id.recyclerViewPokemon)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        loadPokemon()
    }

    private fun loadPokemon() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        lifecycleScope.launch {

            val database =
                PokemonDatabase.getDatabase(
                    requireContext()
                )

            val pokemonList =
                database.pokemonDao().getPokemonByUser(userId)

            if (!isAdded) return@launch

            if (::adapter.isInitialized) {
                adapter.updateData(pokemonList)
            } else {
                adapter = PokemonAdapter(
                    pokemonList,
                    onDeleteClick = { /* fitur hapus menyusul */ },
                    onItemClick = { pokemon -> showFeedDialog(pokemon) }
                )
                recyclerView.adapter = adapter
            }
        }
    }

    /** Tampilkan daftar makanan yang dipunya user (dibeli dari Shop) buat dipilih. */
    private fun showFeedDialog(pokemon: PokemonEntity) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val inventory = db.userInventoryDao().getUserInventory(userId)

            val foodOptions = mutableListOf<Pair<UserInventory, Post>>()
            for (item in inventory) {
                if (item.quantity <= 0) continue
                val post = db.postDao().getPostById(item.postId) ?: continue
                if (post.category == "Makanan") {
                    foodOptions.add(item to post)
                }
            }

            if (!isAdded) return@launch

            if (foodOptions.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Kamu belum punya makanan, beli dulu di Shop",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val labels = foodOptions.map { (item, post) -> "${post.title} (x${item.quantity})" }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Beri makan ${pokemon.name}")
                .setItems(labels) { _, index ->
                    val (item, post) = foodOptions[index]
                    feedPokemon(pokemon, item, post)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun feedPokemon(pokemon: PokemonEntity, inventoryItem: UserInventory, food: Post) {
        lifecycleScope.launch {
            val appDb = AppDatabase.getDatabase(requireContext())
            val pokemonDb = PokemonDatabase.getDatabase(requireContext())

            // Kurangi stok makanan di inventory user
            inventoryItem.quantity -= 1
            appDb.userInventoryDao().update(inventoryItem)

            // Tambah EXP berdasarkan harga makanan, cek naik level (bisa lompat lebih dari
            // 1 level kalau EXP-nya banyak)
            val expGain = calculateExpGain(food.price)
            var exp = pokemon.exp + expGain
            var level = pokemon.level
            var leveledUp = false

            while (exp >= expThreshold(level)) {
                exp -= expThreshold(level)
                level += 1
                leveledUp = true
            }

            val updated = pokemon.copy(exp = exp, level = level)
            pokemonDb.pokemonDao().updatePokemon(updated)

            if (isAdded) {
                val message = if (leveledUp) {
                    "${pokemon.name} diberi makan ${food.title}! Naik ke Level $level!"
                } else {
                    "${pokemon.name} diberi makan ${food.title}! +$expGain EXP"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadPokemon() // refresh biar level & HP di UI ke-update
            }
        }
    }

    /** EXP sebanding sama harga makanan: harga / EXP_DIVISOR, dibatasi MIN..MAX biar gak ekstrem. */
    private fun calculateExpGain(price: Double): Int {
        val raw = (price / EXP_DIVISOR).toInt()
        return raw.coerceIn(MIN_EXP_GAIN, MAX_EXP_GAIN)
    }

    private fun expThreshold(level: Int): Int = level * 20

    companion object {
        private const val EXP_DIVISOR = 5.0
        private const val MIN_EXP_GAIN = 5
        private const val MAX_EXP_GAIN = 100
    }
}