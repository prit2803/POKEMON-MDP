package com.example.proyek_mdp.UI.Payment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.MainActivity
import com.example.proyek_mdp.R
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.PaymentHistory
import kotlinx.coroutines.launch

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var btnFinish: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment_success)

        btnFinish = findViewById(R.id.btnFinish)

        val coin = intent.getIntExtra("coin",0)
        val price = intent.getIntExtra("price",0)
        val method = intent.getStringExtra("method") ?: ""

        saveTransaction(coin,price,method)

        btnFinish.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finishAffinity()

        }

    }

    private fun saveTransaction(

        coin:Int,

        price:Int,

        method:String

    ){

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@PaymentSuccessActivity)

            val userId =
                SessionManager(this@PaymentSuccessActivity)
                    .getUserId()

            val user =
                db.userDao().getUserById(userId)

            if(user!=null){

                user.coins += coin

                db.userDao().update(user)

                db.paymentHistoryDao().insert(

                    PaymentHistory(

                        userId=user.id,

                        paymentMethod=method,

                        coinAmount=coin,

                        totalPrice=price,

                        status="SUCCESS",

                        transactionDate=System.currentTimeMillis()

                    )

                )

            }

        }

    }

}