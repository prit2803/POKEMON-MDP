package com.example.proyek_mdp.UI.Payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.proyek_mdp.R
import java.text.NumberFormat
import java.util.Locale

class PaymentPageActivity : AppCompatActivity() {
    private val viewModel: PaymentViewModel by viewModels { PaymentViewModelFactory( MidtransRepository() ) }
    private lateinit var btnBack: ImageButton

    private lateinit var imgMethod: ImageView

    private lateinit var tvMethod: TextView
    private lateinit var tvNumber: TextView

    private lateinit var tvCoin: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnPaid: Button

    private var coin = 0
    private lateinit var method: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment_page)

        btnBack = findViewById(R.id.btnBack)

        imgMethod = findViewById(R.id.imgMethod)

        tvMethod = findViewById(R.id.tvMethod)
        tvNumber = findViewById(R.id.tvNumber)
        tvCoin = findViewById(R.id.tvCoin)

        btnCopy = findViewById(R.id.btnCopy)
        btnPaid = findViewById(R.id.btnPaid)

        coin = intent.getIntExtra("coin_amount",0)
        method = intent.getStringExtra("payment_method") ?: ""

        tvMethod.text = method

        tvCoin.text =
            NumberFormat.getNumberInstance(Locale("in","ID"))
                .format(coin) + " Coin"

        when(method){

            "BCA"->{
                tvMethod.text = method
                tvNumber.text="88081234567890"

            }

            "GoPay"->{

                tvMethod.text = method
                tvNumber.text="QRIS-GOPAY-123456"

            }

            "OVO"->{

                tvMethod.text = method
                tvNumber.text="081234567890"

            }

            "DANA"->{

                tvMethod.text = method
                tvNumber.text="QRIS-DANA-987654"

            }

            "ShopeePay"->{

                tvMethod.text = method
                tvNumber.text="QRIS-SHOPEEPAY-111"

            }

        }

        btnBack.setOnClickListener {

            finish()

        }

        btnCopy.setOnClickListener {

            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    "payment",
                    tvNumber.text.toString()
                )
            )

            Toast.makeText(
                this,
                "Disalin",
                Toast.LENGTH_SHORT
            ).show()

        }

        btnPaid.setOnClickListener {

            viewModel.createTransaction(

                coin = coin,

                price = coin * 1000L

            )

        }
        lifecycleScope.launch {

            viewModel.transaction.collectLatest { result ->

                result?.onSuccess {

                    val intent = Intent(
                        this@PaymentPageActivity,
                        PaymentWebViewActivity::class.java
                    )

                    intent.putExtra(
                        "redirect_url",
                        it.redirectUrl
                    )

                    intent.putExtra(
                        "order_id",
                        it.orderId
                    )

                    intent.putExtra(
                        "coin_amount",
                        coin
                    )

                    intent.putExtra(
                        "price_rupiah",
                        coin * 1000L
                    )

                    startActivity(intent)

                }

                result?.onFailure {

                    Toast.makeText(
                        this@PaymentPageActivity,
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()

                }

            }

        }
    }

}