package com.example.proyek_mdp.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.AppDatabase
import kotlinx.coroutines.launch

class ManagePostsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_posts, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.rvPosts)
        val btnAddPost = view.findViewById<Button>(R.id.btnAddPostTop)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val db = AppDatabase.getDatabase(requireContext())
        val adapter = PostAdapter(onItemLongClick = { post ->
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Post")
                .setMessage("Hapus \"${post.title}\"?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        db.postDao().deletePost(post)
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        })
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            db.postDao().getAllPosts().collect { posts ->
                adapter.submitList(posts)
            }
        }

        btnAddPost.setOnClickListener {
            (activity as? AdminActivity)?.loadFragment(UploadPostFragment())
        }

        return view
    }
}