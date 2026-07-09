package com.example.proyek_mdp.UI.Payment

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.proyek_mdp.R

class PaymentMethodActivity : AppCompatActivity() {

    private var coinAmount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        coinAmount = intent.getIntExtra("coin_amount", 0)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.layoutBCA).setOnClickListener {
            openProcess("BCA")
        }

        findViewById<LinearLayout>(R.id.layoutMandiri).setOnClickListener {
            openProcess("Mandiri")
        }

        findViewById<LinearLayout>(R.id.layoutBNI).setOnClickListener {
            openProcess("BNI")
        }

        findViewById<LinearLayout>(R.id.layoutBRI).setOnClickListener {
            openProcess("BRI")
        }

        findViewById<LinearLayout>(R.id.layoutOVO).setOnClickListener {
            openProcess("OVO")
        }

        findViewById<LinearLayout>(R.id.layoutGopay).setOnClickListener {
            openProcess("GoPay")
        }

        findViewById<LinearLayout>(R.id.layoutDana).setOnClickListener {
            openProcess("DANA")
        }

        findViewById<LinearLayout>(R.id.layoutShopee).setOnClickListener {
            openProcess("ShopeePay")
        }
    }

    private fun openProcess(method: String) {

        val intent = Intent(this, PaymentPageActivity::class.java)

        intent.putExtra("coin_amount", coinAmount)
        intent.putExtra("payment_method", method)

        startActivity(intent)
    }
}