package com.example.proyek_mdp.UI.Shop

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.proyek_mdp.UI.TopUp.TopUpActivity

class CoinNotEnoughDialog(
    private val currentCoin: Int,
    private val price: Int
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(requireContext())
            .setTitle("Coin Tidak Cukup")
            .setMessage(
                "Coin kamu : $currentCoin Coin\n\n" +
                        "Harga Pokemon : $price Coin\n\n" +
                        "Silakan lakukan Top Up terlebih dahulu."
            )
            .setNegativeButton("Batal", null)

            .setPositiveButton("Top Up") { _, _ ->

                // Tutup dialog ini
                dismiss()

                // Tutup ShopDialogFragment
                (parentFragment as? DialogFragment)?.dismiss()

                // Buka halaman Top Up
                val intent = Intent(
                    requireContext(),
                    TopUpActivity::class.java
                )

                startActivity(intent)
            }

            .create()
    }
}