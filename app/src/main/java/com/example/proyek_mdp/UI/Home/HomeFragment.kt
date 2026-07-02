package com.example.proyek_mdp.UI.Home

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Shop.ShopDialogFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnOpenShop = view.findViewById<Button>(R.id.btnOpenShop)
        btnOpenShop.setOnClickListener {
            ShopDialogFragment().show(childFragmentManager, "shop")
        }
    }
}