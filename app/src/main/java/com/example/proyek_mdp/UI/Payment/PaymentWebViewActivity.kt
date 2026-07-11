package com.example.proyek_mdp.UI.Payment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.Data.local.database.AppDatabase
import com.example.proyek_mdp.Data.local.entity.PaymentHistory
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Network.Midtrans.MidtransClient
import com.example.proyek_mdp.auth.SessionManager
import kotlinx.coroutines.launch

/**
 * PAYMENT WEBVIEW ACTIVITY
 * ========================
 * Ini GANTIIN PaymentMethodActivity + PaymentPageActivity + PaymentProcessActivity
 * punya Eri buat jalur Midtrans ASLI. "redirect_url" yang dibuka di WebView ini
 * adalah HALAMAN ASLI Midtrans (Snap) -- di situ user beneran milih metode
 * (BCA VA, GoPay, dll) dan Midtrans sendiri yang nampilin nomor VA/QR-nya.
 *
 * Kita pantau URL WebView-nya. Begitu user selesai (apapun hasilnya), Midtrans
 * redirect ke URL "finish" yang kita set di MidtransModels.kt (Callbacks).
 * Pas ketauan udah nyampe situ, kita CEK ULANG status transaksi LANGSUNG ke
 * server Midtrans (getTransactionStatus) -- BARU kredit koin kalau statusnya
 * beneran "settlement"/"capture". Ini beda sama versi Eri yang langsung
 * percaya "user klik tombol Sudah Bayar" tanpa verifikasi apapun.
 */
class PaymentWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var alreadyHandled = false // guard biar checkTransactionStatus() gak kepanggil dobel

    private var orderId: String = ""
    private var coinAmount: Int = 0
    private var priceRupiah: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_webview)

        val redirectUrl = intent.getStringExtra("redirect_url") ?: ""
        orderId = intent.getStringExtra("order_id") ?: ""
        coinAmount = intent.getIntExtra("coin_amount", 0)
        priceRupiah = intent.getLongExtra("price_rupiah", 0)

        webView = findViewById(R.id.webViewPayment)
        progressBar = findViewById(R.id.progressBarWeb)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && url.contains("pokemon-mdp.app/finish") && !alreadyHandled) {
                    alreadyHandled = true
                    checkTransactionStatus()
                    return true
                }
                return false
            }
        }

        webView.loadUrl(redirectUrl)
    }

    private fun checkTransactionStatus() {
        lifecycleScope.launch {
            try {
                val response = MidtransClient.coreApi.getTransactionStatus(orderId)

                if (response.isSuccessful && response.body() != null) {
                    val status = response.body()!!
                    val db = AppDatabase.getDatabase(this@PaymentWebViewActivity)

                    when (status.transaction_status) {
                        "settlement", "capture" -> {
                            // Pembayaran BENERAN sukses (udah diverifikasi ke server Midtrans)
                            val userId = SessionManager(this@PaymentWebViewActivity).getUserId()
                            val user = db.userDao().getUserById(userId)

                            if (user != null) {
                                user.coins += coinAmount
                                db.userDao().update(user)

                                db.paymentHistoryDao().insert(
                                    PaymentHistory(
                                        userId = user.id,
                                        paymentMethod = status.payment_type ?: "Midtrans",
                                        coinAmount = coinAmount,
                                        totalPrice = priceRupiah.toInt(),
                                        status = "SUCCESS",
                                        transactionDate = System.currentTimeMillis()
                                    )
                                )
                            }

                            val intent = Intent(this@PaymentWebViewActivity, PaymentSuccessActivity::class.java)
                            intent.putExtra("coin_amount", coinAmount)
                            intent.putExtra("method", status.payment_type ?: "Midtrans")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        "pending" -> {
                            Toast.makeText(
                                this@PaymentWebViewActivity,
                                "Pembayaran masih menunggu, cek lagi di Riwayat nanti",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        else -> {
                            Toast.makeText(this@PaymentWebViewActivity, "Pembayaran gagal/dibatalkan", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this@PaymentWebViewActivity, "Gagal cek status transaksi", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PaymentWebViewActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}