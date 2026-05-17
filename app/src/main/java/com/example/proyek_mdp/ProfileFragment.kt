package com.example.proyek_mdp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tempat untuk mengisi data dummy atau mengambil data dari API/database.
        // Karena data pada fragment_profile.xml sudah diisi langsung di XML,
        // untuk sementara tidak perlu menambahkan kode apa pun di sini.

        // Contoh nanti jika ingin mengubah text secara programmatically:
        // val tvTrainerName = view.findViewById<TextView>(R.id.tvTrainerName)
        // tvTrainerName.text = "Red"
    }
}