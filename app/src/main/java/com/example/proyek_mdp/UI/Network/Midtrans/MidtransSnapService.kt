package com.example.proyek_mdp.UI.Network.Midtrans

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Endpoint buat BIKIN transaksi baru. Base URL-nya beda sama status check (lihat MidtransClient). */
interface MidtransSnapService {
    @POST("transactions")
    suspend fun createTransaction(@Body request: SnapTransactionRequest): Response<SnapTransactionResponse>
}