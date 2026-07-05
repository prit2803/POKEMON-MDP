package com.example.proyek_mdp.UI.Profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyek_mdp.R
import com.example.proyek_mdp.auth.LoginActivity
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.User
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {


    private lateinit var tvTrainerName: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvPokemonCaught: TextView
    private lateinit var tvBattleWon: TextView
    private lateinit var tvDistance: TextView

    private lateinit var btnInventory: Button

    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button

    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        tvTrainerName = view.findViewById(R.id.tvTrainerName)
        tvLevel = view.findViewById(R.id.tvLevel)
        tvPokemonCaught = view.findViewById(R.id.tvPokemonCaught)
        tvBattleWon = view.findViewById(R.id.tvBattleWon)
        tvDistance = view.findViewById(R.id.tvDistance)

        etOldPassword = view.findViewById(R.id.etOldPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)

        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnInventory = view.findViewById(R.id.btnInventory)

        // Kalau session kosong (misal habis clear data), lempar balik ke login
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        loadUserData()

        btnChangePassword.setOnClickListener {
            handleChangePassword()
        }

        btnInventory.setOnClickListener {
            findNavController().navigate(R.id.inventoryFragment)
        }

        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            goToLogin()
        }
    }

    private fun loadUserData() {

        val userId = sessionManager.getUserId()

        if (userId == -1) return

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(requireContext())

            val user =
                db.userDao().getUserById(userId)

            currentUser = user

            if (!isAdded || user == null)
                return@launch

            tvTrainerName.text = user.username

            tvLevel.text = "Level ${user.trainerLevel} Trainer"

            tvPokemonCaught.text = "Pokemon Ditangkap : ${user.pokemonCaught}"

            tvBattleWon.text = "Battle Menang : ${user.battleWon}"

            tvDistance.text = "Jarak Tempuh : ${user.distance} km"
        }
    }
    private fun handleChangePassword() {
        val user = currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Data user belum siap, coba lagi", Toast.LENGTH_SHORT).show()
            return
        }

        val oldPassword = etOldPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (oldPassword != user.password) {
            Toast.makeText(requireContext(), "Password lama salah", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(requireContext(), "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            user.password = newPassword
            db.userDao().update(user)

            if (isAdded) {
                Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                etOldPassword.text.clear()
                etNewPassword.text.clear()
                etConfirmPassword.text.clear()
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }


}