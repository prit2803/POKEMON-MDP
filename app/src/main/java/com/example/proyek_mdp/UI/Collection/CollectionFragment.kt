package com.example.proyek_mdp.UI.Collection

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Database.PokemonEntity
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.Post
import com.example.proyek_mdp.database.UserInventory
import kotlinx.coroutines.launch
import kotlin.math.ceil

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
            GridLayoutManager(requireContext(), 2)

        view.findViewById<TextView>(R.id.btnDeleteAll).setOnClickListener {
            confirmDeleteAll()
        }

        view.findViewById<TextView>(R.id.btnOpenPokedex).setOnClickListener {
            PokedexDialogFragment().show(childFragmentManager, "pokedex")
        }

        loadPokemon()
    }

    private fun confirmDeleteAll() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Semua Pokemon")
            .setMessage("Semua pokemon yang TIDAK terkunci akan dihapus. Pokemon yang di-lock akan tetap aman. Lanjutkan?")
            .setPositiveButton("Hapus Semua") { _, _ ->
                lifecycleScope.launch {
                    val database = PokemonDatabase.getDatabase(requireContext())
                    database.pokemonDao().deleteAllUnlockedByUser(userId)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Pokemon yang tidak terkunci berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadPokemon()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleLock(pokemon: PokemonEntity) {
        lifecycleScope.launch {
            val database = PokemonDatabase.getDatabase(requireContext())
            val newLockState = if (pokemon.isLocked == 1) 0 else 1
            database.pokemonDao().updatePokemon(pokemon.copy(isLocked = newLockState))

            if (isAdded) {
                val message = if (newLockState == 1) "${pokemon.name} dikunci" else "${pokemon.name} dibuka kuncinya"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadPokemon()
            }
        }
    }

    private fun confirmDeleteSingle(pokemon: PokemonEntity) {
        if (pokemon.isLocked == 1) {
            Toast.makeText(requireContext(), "${pokemon.name} terkunci, gak bisa dihapus", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pokemon")
            .setMessage("Hapus ${pokemon.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    val database = PokemonDatabase.getDatabase(requireContext())
                    database.pokemonDao().deletePokemon(pokemon)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "${pokemon.name} dihapus", Toast.LENGTH_SHORT).show()
                        loadPokemon()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
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
                    onItemClick = { pokemon -> showFeedDialog(pokemon) },
                    onItemLongClick = { pokemon -> showActionMenu(pokemon) }
                )
                recyclerView.adapter = adapter
            }
        }
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
                    toggleLock(pokemon) // satu-satunya opsi: buka kunci
                } else {
                    when (index) {
                        0 -> toggleLock(pokemon)
                        1 -> confirmDeleteSingle(pokemon)
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
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

    /**
     * EXP naik bertahap per kelipatan Rp5 harga makanan:
     * harga 1-5 -> +3, 6-10 -> +5, 11-15 -> +7, 16-20 -> +9, dst (naik 2 tiap kelipatan 5).
     * Gak ada batas atas, jadi makanan yang jauh lebih mahal EXP-nya jauh lebih besar juga.
     */
    private fun calculateExpGain(price: Double): Int {
        val bracket = ceil(price / BRACKET_SIZE).toInt().coerceAtLeast(1)
        return BASE_EXP + (bracket - 1) * STEP_EXP
    }

    private fun expThreshold(level: Int): Int = level * 20

    companion object {
        private const val BRACKET_SIZE = 5.0
        private const val BASE_EXP = 3
        private const val STEP_EXP = 2
    }
}