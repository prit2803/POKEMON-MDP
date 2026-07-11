package com.example.proyek_mdp.UI.Network.Midtrans

/**
 * Model-model ini bentuknya mengikuti format JSON yang diminta/dibalikin
 * oleh API Midtrans Snap & Core API. Nama field HARUS PERSIS sama kayak
 * dokumentasi Midtrans (huruf besar/kecilnya, underscore-nya), soalnya Gson
 * mencocokkan berdasarkan nama field ini.
 */

// ===== Request buat bikin transaksi baru (POST ke Snap API) =====
data class SnapTransactionRequest(
    val transaction_details: TransactionDetails,
    val item_details: List<ItemDetail>,
    val customer_details: CustomerDetails? = null,
    val callbacks: Callbacks? = null
)

data class TransactionDetails(
    val order_id: String,   // HARUS unik tiap transaksi, gak boleh sama 2x
    val gross_amount: Long  // total harga dalam Rupiah (bukan koin)
)

data class ItemDetail(
    val id: String,
    val price: Long,
    val quantity: Int,
    val name: String
)

data class CustomerDetails(
    val first_name: String,
    val email: String? = null
)

// "finish" ini URL dummy yang kita pantau sendiri di WebView (lihat
// PaymentWebViewActivity) buat tau kapan user selesai di halaman Midtrans.
// URL ini gak perlu beneran valid/online, cuma dipakai sebagai "penanda".
data class Callbacks(
    val finish: String = "https://pokemon-mdp.app/finish"
)

// ===== Response dari Snap API setelah bikin transaksi =====
data class SnapTransactionResponse(
    val token: String,
    val redirect_url: String
)

// ===== Response dari Core API pas cek status transaksi =====
data class TransactionStatusResponse(
    val transaction_status: String, // "settlement" / "capture" / "pending" / "deny" / "cancel" / "expire"
    val order_id: String,
    val gross_amount: String,
    val payment_type: String? = null // "bank_transfer", "gopay", "qris", dll
)