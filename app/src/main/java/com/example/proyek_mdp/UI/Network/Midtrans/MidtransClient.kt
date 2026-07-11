package com.example.proyek_mdp.UI.Network.Midtrans

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Midtrans butuh Server Key dikirim lewat HTTP Basic Auth di header
 * "Authorization", formatnya: Base64("SERVER_KEY:") (titik dua di akhir,
 * password-nya emang sengaja dikosongin). Credentials.basic() dari OkHttp
 * yang ngurusin encoding Base64-nya otomatis.
 *
 * Interceptor ini "nyusup" ke SETIAP request yang lewat client ini, nempelin
 * header Authorization otomatis -- jadi kita gak perlu nulis header manual
 * tiap manggil API.
 */
private val authInterceptor = Interceptor { chain ->
    val credential = Credentials.basic(MidtransConfig.SERVER_KEY, "")
    val request = chain.request().newBuilder()
        .addHeader("Authorization", credential)
        .addHeader("Accept", "application/json")
        .addHeader("Content-Type", "application/json")
        .build()
    chain.proceed(request)
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .build()

object MidtransClient {

    // Buat BIKIN transaksi baru (base URL: snap/v1)
    val snapApi: MidtransSnapService by lazy {
        Retrofit.Builder()
            .baseUrl(MidtransConfig.SNAP_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MidtransSnapService::class.java)
    }

    // Buat CEK STATUS transaksi (base URL beda: api/v2, makanya Retrofit-nya dipisah)
    val coreApi: MidtransCoreService by lazy {
        Retrofit.Builder()
            .baseUrl(MidtransConfig.CORE_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MidtransCoreService::class.java)
    }
}