package com.example.proyek_mdp.UI.TopUp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Payment.PaymentMethodActivity

class TopUpActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton

    private lateinit var btnTabInstant: Button
    private lateinit var btnTabNominal: Button

    private lateinit var layoutInstant: LinearLayout
    private lateinit var layoutNominal: LinearLayout

    private lateinit var card100: CardView
    private lateinit var card500: CardView
    private lateinit var card1000: CardView

    private lateinit var etNominal: EditText
    private lateinit var btnTopUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)

        btnBack = findViewById(R.id.btnBack)

        btnTabInstant = findViewById(R.id.btnTabInstant)
        btnTabNominal = findViewById(R.id.btnTabNominal)

        layoutInstant = findViewById(R.id.layoutInstant)
        layoutNominal = findViewById(R.id.layoutNominal)

        card100 = findViewById(R.id.card100)
        card500 = findViewById(R.id.card500)
        card1000 = findViewById(R.id.card1000)

        etNominal = findViewById(R.id.etNominal)
        btnTopUp = findViewById(R.id.btnTopUp)

        //----------------------------------
        // BACK
        //----------------------------------

        btnBack.setOnClickListener {
            finish()
        }

        //----------------------------------
        // TAB INSTAN
        //----------------------------------

        btnTabInstant.setOnClickListener {

            layoutInstant.visibility = LinearLayout.VISIBLE
            layoutNominal.visibility = LinearLayout.GONE

            btnTabInstant.setBackgroundResource(R.drawable.bg_topup_selected)
            btnTabNominal.setBackgroundResource(R.drawable.bg_topup_unselected)

        }

        //----------------------------------
        // TAB NOMINAL
        //----------------------------------

        btnTabNominal.setOnClickListener {

            layoutInstant.visibility = LinearLayout.GONE
            layoutNominal.visibility = LinearLayout.VISIBLE

            btnTabInstant.setBackgroundResource(R.drawable.bg_topup_unselected)
            btnTabNominal.setBackgroundResource(R.drawable.bg_topup_selected)

        }

        //----------------------------------
        // CARD INSTAN
        //----------------------------------

        card100.setOnClickListener {
            openPayment(100)
        }

        card500.setOnClickListener {
            openPayment(500)
        }

        card1000.setOnClickListener {
            openPayment(1000)
        }

        //----------------------------------
        // TOPUP MANUAL
        //----------------------------------

        btnTopUp.setOnClickListener {

            val nominal = etNominal.text.toString()

            if (nominal.isEmpty()) {
                Toast.makeText(this, "Masukkan jumlah coin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val coin = nominal.toInt()

            if (coin <= 0) {
                Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            openPayment(coin)

        }

    }

    /**
     * Membuka halaman pemilihan metode pembayaran
     */
    private fun openPayment(coin: Int) {

        val intent = Intent(this, PaymentMethodActivity::class.java)

        intent.putExtra("coin_amount", coin)

        startActivity(intent)

    }

}