package com.example.proyek_mdp.Data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendRetrofitClient {

    // Gunakan IP WiFi PC (192.168.10.6) karena aplikasi dijalankan di HP fisik, bukan emulator
    private const val BASE_URL = "http://100.90.187.68:3000/"

    val api: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)
    }
}
