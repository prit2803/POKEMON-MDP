package com.example.proyek_mdp.UI.History

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        btnBack = findViewById(R.id.btnBack)

        adapter = HistoryAdapter()

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }

        loadHistory()
    }

    private fun loadHistory() {

        val userId = SessionManager(this).getUserId()

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(this@HistoryActivity)

            val history =
                db.paymentHistoryDao().getHistory(userId)

            adapter.submitList(history)

        }

    }

}