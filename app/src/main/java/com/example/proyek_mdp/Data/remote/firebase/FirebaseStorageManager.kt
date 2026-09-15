package com.example.proyek_mdp.Data.remote.firebase

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

/**
 * Helper untuk upload file gambar ke Firebase Storage.
 *
 * Semua fungsi aman dipanggil walau Firebase belum dikonfigurasi: jika
 * google-services.json belum ada, [isAvailable] bernilai false dan fungsi
 * upload mengembalikan null sehingga aplikasi tetap bisa fallback ke penyimpanan lokal.
 */
object FirebaseStorageManager {

    /** Cek apakah Firebase sudah ter-initialize (google-services.json sudah terpasang). */
    fun isAvailable(context: Context): Boolean =
        FirebaseApp.getApps(context).isNotEmpty()

    private fun storage(): FirebaseStorage = FirebaseStorage.getInstance()

    /**
     * Upload gambar dari [localUri] ke folder [folder] di Firebase Storage.
     * @return download URL (https) jika berhasil, atau null jika Firebase tidak tersedia.
     */
    suspend fun uploadImage(
        context: Context,
        localUri: Uri,
        folder: String = "posts"
    ): String? {
        if (!isAvailable(context)) return null
        val ref = buildRef(folder, "jpg")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Upload byte array (mis. hasil kompresi) ke Firebase Storage.
     * @return download URL (https) jika berhasil, atau null jika Firebase tidak tersedia.
     */
    suspend fun uploadBytes(
        context: Context,
        bytes: ByteArray,
        folder: String = "posts",
        extension: String = "jpg"
    ): String? {
        if (!isAvailable(context)) return null
        val ref = buildRef(folder, extension)
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    /** Hapus file dari Firebase Storage berdasarkan download URL-nya. */
    suspend fun deleteByUrl(context: Context, downloadUrl: String) {
        if (!isAvailable(context)) return
        try {
            storage().getReferenceFromUrl(downloadUrl).delete().await()
        } catch (_: Exception) {
            // Abaikan: file mungkin sudah tidak ada / URL bukan dari Firebase Storage.
        }
    }

    private fun buildRef(folder: String, extension: String): StorageReference =
        storage().reference
            .child(folder)
            .child("${System.currentTimeMillis()}_${(0..9999).random()}.$extension")
}
