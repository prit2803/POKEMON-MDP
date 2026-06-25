package com.example.proyek_mdp.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {

            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (
                username.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Semua data harus diisi",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (username.lowercase() == "admin") {

                Toast.makeText(
                    this,
                    "Username admin tidak boleh digunakan",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                Toast.makeText(
                    this,
                    "Format email tidak valid",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (password.length < 8) {

                Toast.makeText(
                    this,
                    "Password minimal 8 karakter",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (password != confirmPassword) {

                Toast.makeText(
                    this,
                    "Password dan Confirm Password tidak sama",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            lifecycleScope.launch {

                val db = AppDatabase.getDatabase(this@RegisterActivity)

                val exists =
                    db.userDao().isUsernameExists(username)

                if (exists > 0) {

                    runOnUiThread {

                        Toast.makeText(
                            this@RegisterActivity,
                            "Username sudah digunakan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return@launch
                }

                db.userDao().insert(
                    User(
                        username = username,
                        email = email,
                        password = password
                    )
                )

                runOnUiThread {

                    Toast.makeText(
                        this@RegisterActivity,
                        "Registrasi berhasil",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}