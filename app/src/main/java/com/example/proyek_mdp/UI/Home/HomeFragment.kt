package com.example.proyek_mdp.UI.Home

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.HomeInfoAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Shop.ShopDialogFragment
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.Post
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvGreeting: TextView
    private lateinit var rvInfoTerbaru: RecyclerView
    private lateinit var rvInfoLainnya: RecyclerView
    private lateinit var tvEmptyTerbaru: TextView
    private lateinit var tvEmptyLainnya: TextView
    private lateinit var layoutStarterCard: View
    private lateinit var imgStarter: ImageView
    private lateinit var tvStarterName: TextView
    private lateinit var tvStarterLevel: TextView

    private lateinit var terbaruAdapter: HomeInfoAdapter
    private lateinit var lainnyaAdapter: HomeInfoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        rvInfoTerbaru = view.findViewById(R.id.rvInfoTerbaru)
        rvInfoLainnya = view.findViewById(R.id.rvInfoLainnya)
        tvEmptyTerbaru = view.findViewById(R.id.tvEmptyTerbaru)
        tvEmptyLainnya = view.findViewById(R.id.tvEmptyLainnya)
        layoutStarterCard = view.findViewById(R.id.layoutStarterCard)
        imgStarter = view.findViewById(R.id.imgStarter)
        tvStarterName = view.findViewById(R.id.tvStarterName)
        tvStarterLevel = view.findViewById(R.id.tvStarterLevel)

        val onCardClick: (Post) -> Unit = { post ->
            ShopDialogFragment.newInstance(post.id).show(childFragmentManager, "shop")
        }

        terbaruAdapter = HomeInfoAdapter(onCardClick)
        lainnyaAdapter = HomeInfoAdapter(onCardClick)

        rvInfoTerbaru.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rvInfoTerbaru.adapter = terbaruAdapter

        rvInfoLainnya.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rvInfoLainnya.adapter = lainnyaAdapter

        view.findViewById<Button>(R.id.btnOpenShop).setOnClickListener {
            ShopDialogFragment.newInstance().show(childFragmentManager, "shop")
        }

        setupGreeting()
        loadInfoSections()
        loadStarter()
    }

    private fun setupGreeting() {
        val username = SessionManager(requireContext()).getUsername()
        tvGreeting.text = if (!username.isNullOrEmpty()) "Halo, $username!" else "Halo, Trainer!"
    }

    private fun loadStarter() {
        val userId = SessionManager(requireContext()).getUserId()
        if (userId == -1) return

        viewLifecycleOwner.lifecycleScope.launch {
            val pokemonDb = PokemonDatabase.getDatabase(requireContext())
            val starter = pokemonDb.pokemonDao().getStarter(userId)

            if (!isAdded) return@launch

            if (starter != null) {
                layoutStarterCard.visibility = View.VISIBLE
                tvStarterName.text = starter.name
                tvStarterLevel.text = "Level ${starter.level}"
                Glide.with(requireContext()).load(starter.imageUrl).into(imgStarter)
            } else {
                layoutStarterCard.visibility = View.GONE
            }
        }
    }

    private fun loadInfoSections() {
        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            db.postDao().getActivePosts().collect { posts ->
                if (!isAdded) return@collect

                // Info Terbaru: semua post aktif, terbaru duluan (query sudah ORDER BY createdAt DESC)
                terbaruAdapter.submitList(posts)
                tvEmptyTerbaru.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE

                // Info Lainnya: khusus kategori "Lainnya"
                val lainnya = posts.filter { it.category == "Lainnya" }
                lainnyaAdapter.submitList(lainnya)
                tvEmptyLainnya.visibility = if (lainnya.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}