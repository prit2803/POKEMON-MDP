package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.AppDatabase
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalUsers)
        val tvBanned = view.findViewById<TextView>(R.id.tvBannedUsers)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            tvTotal.text = "Total Users: ${db.userDao().getTotalUsers()}"
            tvBanned.text = "Banned Users: ${db.userDao().getBannedUsersCount()}"
        }
        return view
    }
}