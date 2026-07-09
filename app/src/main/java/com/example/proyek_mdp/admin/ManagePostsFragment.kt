package com.example.proyek_mdp.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.R
import com.example.proyek_mdp.viewmodel.ManagePostsViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ManagePostsFragment : Fragment() {

    private val viewModel: ManagePostsViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_posts, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.rvPosts)
        val btnAddPost = view.findViewById<Button>(R.id.btnAddPostTop)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val adapter = PostAdapter(onItemLongClick = { post ->
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Post")
                .setMessage("Hapus \"${post.title}\"?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.deletePost(post)
                }
                .setNegativeButton("Batal", null)
                .show()
        })
        rv.adapter = adapter

        viewModel.postsList.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }
        
        viewModel.loadPosts()

        btnAddPost.setOnClickListener {
            (activity as? AdminActivity)?.loadFragment(UploadPostFragment())
        }

        return view
    }
}