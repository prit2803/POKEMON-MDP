package com.example.proyek_mdp.UI.Payment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyek_mdp.MainActivity
import com.example.proyek_mdp.R

/**
 * PAYMENT SUCCESS ACTIVITY
 * ========================
 * PENTING: koin & PaymentHistory udah di-simpan di PaymentWebViewActivity
 * (SETELAH status transaksi diverifikasi beneran "settlement" ke Midtrans).
 * Activity ini CUMA nampilin ringkasan -- gak boleh nyimpen/nambah koin lagi
 * di sini, soalnya kalau ditambah lagi bakal DOBEL KREDIT (ini bug yang ada
 * di versi sebelumnya, sekarang udah dihapus).
 */
class PaymentSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        // Key ini HARUS SAMA PERSIS kayak yang dikirim dari PaymentWebViewActivity:
        // intent.putExtra("coin_amount", coinAmount) dan intent.putExtra("method", ...)
        val coinAmount = intent.getIntExtra("coin_amount", 0)
        val method = intent.getStringExtra("method") ?: "Midtrans"

        findViewById<TextView>(R.id.tvSuccessCoin).text = "+$coinAmount Coin"
        findViewById<TextView>(R.id.tvSuccessMethod).text = "via $method"

        findViewById<Button>(R.id.btnFinish).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}