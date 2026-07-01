package com.example.proyek_mdp.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.proyek_mdp.R
// TODO WAJIB DIISI: sesuaikan import ini dengan package Activity login admin kamu
import com.example.proyek_mdp.auth.LoginActivity

class AdminProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_profile, container, false)

        val tvUsername = view.findViewById<TextView>(R.id.tvAdminUsername)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // TODO: ganti "session_prefs" & "username" sesuai nama SharedPreferences
        // yang benar-benar dipakai di LoginActivity kamu (lihat file itu untuk pastikan).
        val prefs = requireContext().getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
        tvUsername.text = prefs.getString("username", "Admin")

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Logout") { _, _ ->
                    // Hapus session
                    prefs.edit().clear().apply()

                    // TODO WAJIB DIISI: pastikan import LoginActivity di atas mengarah ke
                    // class Activity login admin kamu yang sebenarnya.
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        return view
    }
}