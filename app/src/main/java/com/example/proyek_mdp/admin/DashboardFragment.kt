package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.AppDatabase
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalUsers)
        val tvBanned = view.findViewById<TextView>(R.id.tvBannedUsers)
        val tvTotalPosts = view.findViewById<TextView>(R.id.tvTotalPosts)
        val btnAddPost = view.findViewById<Button>(R.id.btnAddPost)
        val btnManagePosts = view.findViewById<Button>(R.id.btnManagePosts)

        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                tvTotal.text = "Total Users: ${db.userDao().getTotalUsers()}"
                tvBanned.text = "Banned Users: ${db.userDao().getBannedUsersCount()}"
                tvTotalPosts.text = "Total Post/Promo: ${db.postDao().getTotalPosts()}"
            } catch (e: Exception) {
                tvTotal.text = "Total Users: —"
                tvBanned.text = "Banned Users: —"
                tvTotalPosts.text = "Total Post/Promo: —"
            }
        }

        btnAddPost.setOnClickListener {
            (activity as? AdminActivity)?.loadFragment(UploadPostFragment())
        }

        btnManagePosts.setOnClickListener {
            (activity as? AdminActivity)?.loadFragment(ManagePostsFragment())
        }

        return view
    }
}