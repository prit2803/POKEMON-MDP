package com.example.proyek_mdp.UI.TopUp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.R
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
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

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_top_up)

        sessionManager = SessionManager(this)

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

        //--------------------------------
        // Back
        //--------------------------------

        btnBack.setOnClickListener {
            finish()
        }

        //--------------------------------
        // TAB INSTAN
        //--------------------------------

        btnTabInstant.setOnClickListener {

            layoutInstant.visibility = android.view.View.VISIBLE
            layoutNominal.visibility = android.view.View.GONE

            btnTabInstant.setBackgroundResource(R.drawable.bg_topup_selected)
            btnTabNominal.setBackgroundResource(R.drawable.bg_topup_unselected)

        }

        //--------------------------------
        // TAB NOMINAL
        //--------------------------------

        btnTabNominal.setOnClickListener {

            layoutInstant.visibility = android.view.View.GONE
            layoutNominal.visibility = android.view.View.VISIBLE

            btnTabInstant.setBackgroundResource(R.drawable.bg_topup_unselected)
            btnTabNominal.setBackgroundResource(R.drawable.bg_topup_selected)

        }

        //--------------------------------
        // CARD
        //--------------------------------

        card100.setOnClickListener {

            topUpCoins(100)

        }

        card500.setOnClickListener {

            topUpCoins(500)

        }

        card1000.setOnClickListener {

            topUpCoins(1000)

        }

        //--------------------------------
        // TOPUP MANUAL
        //--------------------------------

        btnTopUp.setOnClickListener {

            val jumlah = etNominal.text.toString()

            if (jumlah.isEmpty())
                return@setOnClickListener

            topUpCoins(jumlah.toInt())

        }

    }

    //-------------------------------------------------

    private fun topUpCoins(amount: Int) {

        val userId = sessionManager.getUserId()

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@TopUpActivity)

            val user = db.userDao().getUserById(userId)

            if (user != null) {

                user.coins += amount

                db.userDao().update(user)

            }

            finish()

        }

    }

}