package com.example.proyek_mdp.UI.Payment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import kotlinx.coroutines.launch
import com.example.proyek_mdp.R

class PaymentProcessActivity : AppCompatActivity() {
    private lateinit var tvMethod: TextView
    private lateinit var tvCoin: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_process)
        sessionManager = SessionManager(this)
        tvMethod = findViewById(R.id.tvMethod)
        tvCoin = findViewById(R.id.tvCoin)

        val method = intent.getStringExtra("payment_method") ?: ""
        val coin = intent.getIntExtra("coin_amount",0)

        tvMethod.text = method
        tvCoin.text = "$coin Coin"

        Handler(Looper.getMainLooper()).postDelayed({

            lifecycleScope.launch {

                val db = AppDatabase.getDatabase(this@PaymentProcessActivity)

                val user =
                    db.userDao().getUserById(sessionManager.getUserId())

                if (user != null) {

                    // Tambah coin
                    user.coins += coin

                    // Simpan ke Room
                    db.userDao().update(user)
                }

                val intent = Intent(
                    this@PaymentProcessActivity,
                    PaymentSuccessActivity::class.java
                )

                intent.putExtra("payment_method", method)
                intent.putExtra("coin_amount", coin)

                startActivity(intent)

                finish()

            }

        },3000)
    }
}