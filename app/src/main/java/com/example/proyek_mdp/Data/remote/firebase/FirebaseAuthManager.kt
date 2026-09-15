package com.example.proyek_mdp.Data.remote.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Wrapper Firebase Authentication (email/password).
 *
 * Aman dipanggil walau Firebase belum dikonfigurasi: [isAvailable] akan false
 * dan operasi mengembalikan [AuthResult] dengan success=false, sehingga alur
 * login/register lama (backend MySQL) tetap dapat dipakai sebagai fallback.
 */
object FirebaseAuthManager {

    data class AuthResult(
        val success: Boolean,
        val uid: String? = null,
        val error: String? = null
    )

    fun isAvailable(context: Context): Boolean =
        FirebaseApp.getApps(context).isNotEmpty()

    private fun auth(): FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun register(context: Context, email: String, password: String): AuthResult {
        if (!isAvailable(context)) return AuthResult(false, error = "Firebase belum dikonfigurasi")
        return try {
            val result = auth().createUserWithEmailAndPassword(email, password).await()
            AuthResult(true, result.user?.uid)
        } catch (e: Exception) {
            AuthResult(false, error = e.message)
        }
    }

    suspend fun login(context: Context, email: String, password: String): AuthResult {
        if (!isAvailable(context)) return AuthResult(false, error = "Firebase belum dikonfigurasi")
        return try {
            val result = auth().signInWithEmailAndPassword(email, password).await()
            AuthResult(true, result.user?.uid)
        } catch (e: Exception) {
            AuthResult(false, error = e.message)
        }
    }

    fun currentUid(): String? =
        runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()

    fun logout() {
        runCatching { FirebaseAuth.getInstance().signOut() }
    }
}
