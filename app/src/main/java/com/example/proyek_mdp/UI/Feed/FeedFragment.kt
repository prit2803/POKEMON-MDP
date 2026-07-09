package com.example.proyek_mdp.UI.Feed

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Shop.ShopDialogFragment
import com.example.proyek_mdp.admin.PostAdapter
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.viewmodel.FeedViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var rvFeed: RecyclerView
    private lateinit var adapter: PostAdapter
    private var allActivePosts: List<Post> = emptyList()

    private val viewModel: FeedViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFeed = view.findViewById(R.id.rvFeed)
        rvFeed.layoutManager = LinearLayoutManager(requireContext())

        // User tidak bisa hapus post (onItemLongClick kosong).
        // Klik post -> buka popup Shop, langsung fokus ke item itu.
        adapter = PostAdapter(
            onItemLongClick = {},
            onItemClick = { post ->
                ShopDialogFragment.newInstance(post.id)
                    .show(childFragmentManager, "shop")
            }
        )
        rvFeed.adapter = adapter

        viewModel.activePosts.observe(viewLifecycleOwner) { posts ->
            allActivePosts = posts
        }

        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }

        viewModel.loadActivePosts()

        view.findViewById<Button>(R.id.btnFilterAll).setOnClickListener {
            viewModel.filterByCategory(null)
        }
        view.findViewById<Button>(R.id.btnFilterKartu).setOnClickListener {
            viewModel.filterByCategory("Kartu Pokemon")
        }
        view.findViewById<Button>(R.id.btnFilterMakanan).setOnClickListener {
            viewModel.filterByCategory("Makanan")
        }
        view.findViewById<Button>(R.id.btnFilterLainnya).setOnClickListener {
            viewModel.filterByCategory("Lainnya")
        }
    }
}