package com.example.proyek_mdp.UI.Profile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.R
import com.example.proyek_mdp.auth.LoginActivity
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.User
import kotlinx.coroutines.launch

/**
 * Popup Settings full-screen, dibuka dari ProfileFragment lewat icon gear.
 * Isinya: ganti nickname, ganti password, ganti tim (butuh 5000 koin), dan logout.
 * Setelah ada perubahan, kirim "profile_updated" ke ProfileFragment biar auto-refresh.
 */
class SettingsDialogFragment : DialogFragment(R.layout.fragment_settings_dialog) {

    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    private lateinit var etNickname: EditText
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvCurrentTeamCoins: TextView
    private lateinit var btnTeamMystic: TextView
    private lateinit var btnTeamValor: TextView
    private lateinit var btnTeamInstinct: TextView

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        etNickname = view.findViewById(R.id.etNickname)
        etOldPassword = view.findViewById(R.id.etOldPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        tvCurrentTeamCoins = view.findViewById(R.id.tvCurrentTeamCoins)
        btnTeamMystic = view.findViewById(R.id.btnTeamMystic)
        btnTeamValor = view.findViewById(R.id.btnTeamValor)
        btnTeamInstinct = view.findViewById(R.id.btnTeamInstinct)

        view.findViewById<TextView>(R.id.btnCloseSettings).setOnClickListener { dismiss() }
        view.findViewById<TextView>(R.id.btnSaveNickname).setOnClickListener { handleSaveNickname() }
        view.findViewById<TextView>(R.id.btnChangePassword).setOnClickListener { handleChangePassword() }

        btnTeamMystic.setOnClickListener { confirmChangeTeam("Mystic") }
        btnTeamValor.setOnClickListener { confirmChangeTeam("Valor") }
        btnTeamInstinct.setOnClickListener { confirmChangeTeam("Instinct") }

        view.findViewById<TextView>(R.id.btnLogout).setOnClickListener { handleLogout() }

        loadUser()
    }

    private fun loadUser() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val user = db.userDao().getUserById(userId)
            currentUser = user

            if (isAdded && user != null) {
                etNickname.setText(user.nickname ?: "")
                updateTeamDisplay(user)
            }
        }
    }

    private fun updateTeamDisplay(user: User) {
        val teamLabel = user.team ?: "belum pilih"
        val costInfo = if (user.team == null) {
            "Pilihan pertama gratis"
        } else {
            "Ganti tim butuh $TEAM_CHANGE_COST koin"
        }
        tvCurrentTeamCoins.text = "Tim sekarang: $teamLabel  •  Koin: ${user.coins}  •  $costInfo"

        btnTeamMystic.text = if (user.team == "Mystic") "💧 Mystic ✓" else "💧 Mystic"
        btnTeamValor.text = if (user.team == "Valor") "🔥 Valor ✓" else "🔥 Valor"
        btnTeamInstinct.text = if (user.team == "Instinct") "⚡ Instinct ✓" else "⚡ Instinct"
    }

    private fun handleSaveNickname() {
        val user = currentUser ?: return
        val nickname = etNickname.text.toString().trim()

        if (nickname.isEmpty()) {
            Toast.makeText(requireContext(), "Nickname gak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            user.nickname = nickname
            db.userDao().update(user)

            if (isAdded) {
                Toast.makeText(requireContext(), "Nickname berhasil disimpan", Toast.LENGTH_SHORT).show()
                notifyProfileUpdated()
            }
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

        viewLifecycleOwner.lifecycleScope.launch {
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

    private fun confirmChangeTeam(team: String) {
        val user = currentUser ?: return

        if (user.team == team) {
            Toast.makeText(requireContext(), "Kamu udah di tim $team", Toast.LENGTH_SHORT).show()
            return
        }

        val isFirstPick = user.team == null

        if (isFirstPick) {
            // Pilihan tim pertama kali, gratis, gak perlu konfirmasi biaya
            AlertDialog.Builder(requireContext())
                .setTitle("Pilih Tim $team?")
                .setMessage("Ini pilihan tim pertama kamu, gratis!")
                .setPositiveButton("Pilih") { _, _ -> changeTeam(user, team, cost = 0) }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Ganti ke Tim $team?")
                .setMessage("Ganti tim butuh $TEAM_CHANGE_COST koin. Koin kamu sekarang: ${user.coins}.")
                .setPositiveButton("Ganti") { _, _ -> changeTeam(user, team, cost = TEAM_CHANGE_COST) }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun changeTeam(user: User, team: String, cost: Int) {
        if (user.coins < cost) {
            Toast.makeText(requireContext(), "Koin kamu gak cukup (butuh $cost)", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            user.coins -= cost
            user.team = team
            db.userDao().update(user)

            if (isAdded) {
                updateTeamDisplay(user)
                val message = if (cost == 0) {
                    "Selamat bergabung di Tim $team!"
                } else {
                    "Berhasil pindah ke Tim $team!"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                notifyProfileUpdated()
            }
        }
    }

    private fun handleLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Yakin mau logout?")
            .setPositiveButton("Logout") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /** Kasih tau ProfileFragment (yang manggil dialog ini lewat childFragmentManager) buat refresh data. */
    private fun notifyProfileUpdated() {
        parentFragmentManager.setFragmentResult("profile_updated", Bundle())
    }

    companion object {
        private const val TEAM_CHANGE_COST = 5000
    }
}