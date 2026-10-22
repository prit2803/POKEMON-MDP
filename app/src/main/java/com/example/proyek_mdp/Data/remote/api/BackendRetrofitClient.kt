package com.example.proyek_mdp.Data.remote.api

import com.example.proyek_mdp.Data.remote.firebase.FirestoreApiService

/**
 * Titik akses data cloud aplikasi.
 *
 * Sebelumnya memakai Retrofit ke backend Node + MySQL/TiDB. Sekarang memakai
 * Cloud Firestore ([FirestoreApiService]) supaya tidak perlu server SQL.
 *
 * Seluruh Repository & DAO tetap memakai [BackendApiService] yang sama,
 * jadi tidak ada perubahan di sisi pemanggil.
 */
object BackendRetrofitClient {

    val api: BackendApiService by lazy { FirestoreApiService() }
}
