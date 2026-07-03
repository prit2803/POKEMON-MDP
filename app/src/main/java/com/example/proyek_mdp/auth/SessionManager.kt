package com.example.proyek_mdp.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Penyimpanan sederhana untuk melacak user yang sedang login,
 * menggunakan SharedPreferences. Dipakai oleh Profile (untuk load data)
 * dan Logout (untuk clear session).
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(userId: Int, username: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun isLoggedIn(): Boolean = getUserId() != -1

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }
}