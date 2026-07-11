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
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Network.Midtrans.CustomerDetails
import com.example.proyek_mdp.UI.Network.Midtrans.ItemDetail
import com.example.proyek_mdp.UI.Network.Midtrans.MidtransClient
import com.example.proyek_mdp.UI.Network.Midtrans.MidtransConfig
import com.example.proyek_mdp.UI.Network.Midtrans.SnapTransactionRequest
import com.example.proyek_mdp.UI.Network.Midtrans.TransactionDetails
import com.example.proyek_mdp.UI.Payment.PaymentWebViewActivity
import com.example.proyek_mdp.auth.SessionManager
import kotlinx.coroutines.launch

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
            startMidtransPayment(100)
        }

        card500.setOnClickListener {
            startMidtransPayment(500)
        }

        card1000.setOnClickListener {
            startMidtransPayment(1000)
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

            startMidtransPayment(coin)

        }

    }

    /**
     * DIGANTI dari openPayment() (yang tadinya buka PaymentMethodActivity buatan sendiri)
     * jadi langsung panggil Midtrans Snap API asli. Alur PaymentMethodActivity /
     * PaymentPageActivity / PaymentProcessActivity gak dipakai lagi di jalur ini,
     * soalnya Midtrans Snap sendiri udah nyediain halaman pilih metode + nomor VA/QR.
     */
    private fun startMidtransPayment(coin: Int) {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        if (userId == -1) {
            Toast.makeText(this, "Sesi login gak ketemu, coba login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val priceRupiah = (coin * MidtransConfig.RUPIAH_PER_COIN).toLong()
        val orderId = "TOPUP-$userId-${System.currentTimeMillis()}"
        val username = sessionManager.getUsername() ?: "Trainer"

        val request = SnapTransactionRequest(
            transaction_details = TransactionDetails(
                order_id = orderId,
                gross_amount = priceRupiah
            ),
            item_details = listOf(
                ItemDetail(
                    id = "coin_$coin",
                    price = priceRupiah,
                    quantity = 1,
                    name = "Top Up $coin Coin"
                )
            ),
            customer_details = CustomerDetails(first_name = username)
        )

        lifecycleScope.launch {
            try {
                val response = MidtransClient.snapApi.createTransaction(request)

                if (response.isSuccessful && response.body() != null) {
                    val snapResult = response.body()!!

                    val intent = Intent(this@TopUpActivity, PaymentWebViewActivity::class.java)
                    intent.putExtra("redirect_url", snapResult.redirect_url)
                    intent.putExtra("order_id", orderId)
                    intent.putExtra("coin_amount", coin)
                    intent.putExtra("price_rupiah", priceRupiah)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@TopUpActivity,
                        "Gagal membuat transaksi (${response.code()}). Cek Server Key di MidtransConfig.kt.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TopUpActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}