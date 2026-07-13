package com.example.proyek_mdp.Data.remote.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BackendRetrofitClient {

    // Gunakan IP WiFi PC (192.168.10.6) karena aplikasi dijalankan di HP fisik, bukan emulator
    private const val BASE_URL = "https://photograph-constitute-mounts-mouse.trycloudflare.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()

    val api: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)
    }
}
