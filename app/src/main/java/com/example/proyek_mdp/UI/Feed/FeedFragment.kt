package com.example.proyek_mdp.UI.Feed

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.admin.PostAdapter
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.Post
import kotlinx.coroutines.launch

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var rvFeed: RecyclerView
    private lateinit var adapter: PostAdapter
    private var allActivePosts: List<Post> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFeed = view.findViewById(R.id.rvFeed)
        rvFeed.layoutManager = LinearLayoutManager(requireContext())

        // User tidak bisa hapus post, jadi callback dikosongkan
        adapter = PostAdapter(onItemLongClick = {})
        rvFeed.adapter = adapter

        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            db.postDao().getActivePosts().collect { posts ->
                allActivePosts = posts
                adapter.submitList(posts)
            }
        }

        view.findViewById<Button>(R.id.btnFilterAll).setOnClickListener {
            adapter.submitList(allActivePosts)
        }
        view.findViewById<Button>(R.id.btnFilterKartu).setOnClickListener {
            adapter.submitList(allActivePosts.filter { it.category == "Kartu Pokemon" })
        }
        view.findViewById<Button>(R.id.btnFilterMakanan).setOnClickListener {
            adapter.submitList(allActivePosts.filter { it.category == "Makanan" })
        }
        view.findViewById<Button>(R.id.btnFilterLainnya).setOnClickListener {
            adapter.submitList(allActivePosts.filter { it.category == "Lainnya" })
        }
    }
}