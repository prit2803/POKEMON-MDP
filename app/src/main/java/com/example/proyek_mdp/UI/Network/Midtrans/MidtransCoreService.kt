package com.example.proyek_mdp.UI.Network.Midtrans

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/** Endpoint buat CEK STATUS transaksi (udah dibayar apa belum). Base URL beda sama Snap. */
interface MidtransCoreService {
    @GET("{orderId}/status")
    suspend fun getTransactionStatus(@Path("orderId") orderId: String): Response<TransactionStatusResponse>
}