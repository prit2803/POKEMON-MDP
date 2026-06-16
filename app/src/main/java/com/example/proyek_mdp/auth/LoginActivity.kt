package com.example.proyek_mdp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyek_mdp.R
import com.example.proyek_mdp.admin.AdminActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {

            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Username dan Password harus diisi",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (username == "admin" && password == "admin") {

                Toast.makeText(
                    this,
                    "Login Admin Berhasil",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(
                        this,
                        AdminActivity::class.java
                    )
                )

                finish()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Username atau Password salah",
                Toast.LENGTH_SHORT
            ).show()
        }

        tvRegister.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )

        }
    }
}