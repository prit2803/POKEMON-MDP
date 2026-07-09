package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.R
import com.example.proyek_mdp.viewmodel.DashboardViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalUsers)
        val tvBanned = view.findViewById<TextView>(R.id.tvBannedUsers)
        val tvTotalPosts = view.findViewById<TextView>(R.id.tvTotalPosts)
        val btnAddPost = view.findViewById<Button>(R.id.btnAddPost)
        val btnManagePosts = view.findViewById<Button>(R.id.btnManagePosts)

        viewModel.totalUsers.observe(viewLifecycleOwner) {
            tvTotal.text = "Total Users: $it"
        }
        viewModel.bannedUsers.observe(viewLifecycleOwner) {
            tvBanned.text = "Banned Users: $it"
        }
        viewModel.totalPosts.observe(viewLifecycleOwner) {
            tvTotalPosts.text = "Total Post/Promo: $it"
        }
        viewModel.error.observe(viewLifecycleOwner) {
            tvTotal.text = "Total Users: —"
            tvBanned.text = "Banned Users: —"
            tvTotalPosts.text = "Total Post/Promo: —"
        }

        viewModel.loadDashboardStats()

        btnAddPost.setOnClickListener {
            (activity as? AdminActivity)?.loadFragment(UploadPostFragment())
        }

        btnManagePosts.setOnClickListener {
            (activity as? AdminActivity)?.loadFragment(ManagePostsFragment())
        }

        return view
    }
}