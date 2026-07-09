package com.example.proyek_mdp.UI.Profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyek_mdp.R
import com.example.proyek_mdp.Data.local.database.PokemonDatabase
import com.example.proyek_mdp.auth.LoginActivity
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.Data.local.database.AppDatabase
import com.example.proyek_mdp.Data.local.entity.User
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var tvTrainerName: TextView
    private lateinit var tvUsernameHandle: TextView
    private lateinit var tvStatPokemonCount: TextView
    private lateinit var tvStatCoins: TextView
    private lateinit var tvStatTeam: TextView
    private lateinit var tvBattleWon: TextView
    private lateinit var tvDistance: TextView
    private lateinit var btnInventory: TextView
    private lateinit var btnSettings: TextView

    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        tvTrainerName = view.findViewById(R.id.tvTrainerName)
        tvUsernameHandle = view.findViewById(R.id.tvUsernameHandle)
        tvStatPokemonCount = view.findViewById(R.id.tvStatPokemonCount)
        tvStatCoins = view.findViewById(R.id.tvStatCoins)
        tvStatTeam = view.findViewById(R.id.tvStatTeam)
        tvBattleWon = view.findViewById(R.id.tvBattleWon)
        tvDistance = view.findViewById(R.id.tvDistance)
        btnInventory = view.findViewById(R.id.btnInventory)
        btnSettings = view.findViewById(R.id.btnSettings)

        // Kalau session kosong (misal habis clear data), lempar balik ke login
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // Dengerin sinyal dari SettingsDialogFragment kalau ada data yang berubah
        // (nickname/password/tim), biar Profile auto-refresh pas dialognya ditutup.
        childFragmentManager.setFragmentResultListener("profile_updated", viewLifecycleOwner) { _, _ ->
            loadUserData()
        }

        btnSettings.setOnClickListener {
            SettingsDialogFragment().show(childFragmentManager, "settings")
        }

        btnInventory.setOnClickListener {
            findNavController().navigate(R.id.inventoryFragment)
        }

        loadUserData()
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val user = db.userDao().getUserById(userId)
            currentUser = user

            // Jumlah pokemon diambil LANGSUNG dari database pokemon (real-time),
            // bukan dari angka statis, biar selalu akurat.
            val pokemonDb = PokemonDatabase.getDatabase(requireContext())
            val pokemonCount = pokemonDb.pokemonDao().getPokemonByUser(userId).size

            if (!isAdded || user == null) return@launch

            val displayName = user.nickname?.takeIf { it.isNotBlank() } ?: user.username
            tvTrainerName.text = displayName

            if (!user.nickname.isNullOrBlank()) {
                tvUsernameHandle.text = "@${user.username}"
                tvUsernameHandle.visibility = View.VISIBLE
            } else {
                tvUsernameHandle.visibility = View.GONE
            }

            tvStatPokemonCount.text = pokemonCount.toString()
            tvStatCoins.text = user.coins.toString()
            tvStatTeam.text = user.team ?: "-"

            tvBattleWon.text = "Battle Menang: ${user.battleWon}"
            tvDistance.text = "Jarak Tempuh: ${user.distance} km"
        }
    }

    private fun goToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}