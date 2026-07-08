package com.example.proyek_mdp.UI.Home

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.BannerDisplayItem
import com.example.proyek_mdp.UI.Adapter.HomeBannerAdapter
import com.example.proyek_mdp.UI.Adapter.HomeFeedAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Shop.ShopDialogFragment
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.Post
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvGreeting: TextView

    // Banner carousel
    private lateinit var rvBanner: RecyclerView
    private lateinit var bannerAdapter: HomeBannerAdapter
    private lateinit var dotsContainer: LinearLayout
    private lateinit var tvEmptyBanner: TextView
    private var bannerCount = 0
    private var currentBannerIndex = 0

    // Starter
    private lateinit var layoutStarterCard: View
    private lateinit var imgStarter: ImageView
    private lateinit var tvStarterName: TextView
    private lateinit var tvStarterLevel: TextView

    // Feed + filter (gabungan dari Feed lama)
    private lateinit var rvHomeFeed: RecyclerView
    private lateinit var tvEmptyFeed: TextView
    private lateinit var feedAdapter: HomeFeedAdapter
    private lateinit var btnFilterAll: TextView
    private lateinit var btnFilterKartu: TextView
    private lateinit var btnFilterMakanan: TextView
    private lateinit var btnFilterLainnya: TextView
    private var allActivePosts: List<Post> = emptyList()
    private var currentCategory: String? = null // null = "Semua"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvGreeting = view.findViewById(R.id.tvGreeting)
        layoutStarterCard = view.findViewById(R.id.layoutStarterCard)
        imgStarter = view.findViewById(R.id.imgStarter)
        tvStarterName = view.findViewById(R.id.tvStarterName)
        tvStarterLevel = view.findViewById(R.id.tvStarterLevel)

        val onCardClick: (Post) -> Unit = { post ->
            ShopDialogFragment.newInstance(post.id).show(childFragmentManager, "shop")
        }

        val onBannerClick: (BannerDisplayItem) -> Unit = { banner ->
            // Cuma buka Shop kalau ini banner promo beneran (postId != null).
            // Banner statis/dekoratif (Pikachu, Charizard, dll) gak ngapa-ngapain pas diklik.
            if (banner.postId != null) {
                ShopDialogFragment.newInstance(banner.postId).show(childFragmentManager, "shop")
            }
        }

        // ===== Banner carousel =====
        rvBanner = view.findViewById(R.id.rvBanner)
        dotsContainer = view.findViewById(R.id.dotsContainer)
        tvEmptyBanner = view.findViewById(R.id.tvEmptyBanner)

        val bannerLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rvBanner.layoutManager = bannerLayoutManager
        bannerAdapter = HomeBannerAdapter(onItemClick = onBannerClick)
        rvBanner.adapter = bannerAdapter
        LinearSnapHelper().attachToRecyclerView(rvBanner)

        rvBanner.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = bannerLayoutManager.findFirstVisibleItemPosition()
                    if (pos != RecyclerView.NO_POSITION && pos != currentBannerIndex) {
                        currentBannerIndex = pos
                        updateDots()
                    }
                }
            }
        })

        // ===== Feed + filter kategori =====
        rvHomeFeed = view.findViewById(R.id.rvHomeFeed)
        tvEmptyFeed = view.findViewById(R.id.tvEmptyFeed)
        btnFilterAll = view.findViewById(R.id.btnFilterAll)
        btnFilterKartu = view.findViewById(R.id.btnFilterKartu)
        btnFilterMakanan = view.findViewById(R.id.btnFilterMakanan)
        btnFilterLainnya = view.findViewById(R.id.btnFilterLainnya)

        feedAdapter = HomeFeedAdapter(onCardClick)
        rvHomeFeed.layoutManager = LinearLayoutManager(requireContext())
        rvHomeFeed.adapter = feedAdapter

        btnFilterAll.setOnClickListener { applyFilter(null) }
        btnFilterKartu.setOnClickListener { applyFilter("Kartu Pokemon") }
        btnFilterMakanan.setOnClickListener { applyFilter("Makanan") }
        btnFilterLainnya.setOnClickListener { applyFilter("Lainnya") }

        view.findViewById<View>(R.id.btnOpenShop).setOnClickListener {
            ShopDialogFragment.newInstance().show(childFragmentManager, "shop")
        }

        setupGreeting()
        loadFeed()
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

    private fun loadFeed() {
        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            db.postDao().getActivePosts().collect { posts ->
                if (!isAdded) return@collect

                allActivePosts = posts

                // Banner: gambar featured statis (Pikachu, dll) duluan, disusul promo post admin (maks 8)
                val postBanners = posts.take(8).map { post ->
                    BannerDisplayItem(
                        imageUrl = post.imagePath ?: "",
                        title = post.title,
                        subtitle = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(post.price),
                        postId = post.id
                    )
                }
                val combinedBanners = FEATURED_BANNERS + postBanners

                bannerAdapter.updateData(combinedBanners)
                setupDots(combinedBanners.size)
                tvEmptyBanner.visibility = View.GONE // selalu ada isi karena FEATURED_BANNERS gak pernah kosong
                rvBanner.visibility = View.VISIBLE

                // Terapkan ulang filter yang lagi aktif ke data terbaru
                applyFilter(currentCategory)
            }
        }
    }

    private fun applyFilter(category: String?) {
        currentCategory = category

        val filtered = if (category == null) {
            allActivePosts
        } else {
            allActivePosts.filter { it.category == category }
        }

        feedAdapter.submitList(filtered)
        tvEmptyFeed.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE

        updateFilterButtonStyles()
    }

    private fun updateFilterButtonStyles() {
        val buttons = mapOf(
            btnFilterAll to null,
            btnFilterKartu to "Kartu Pokemon",
            btnFilterMakanan to "Makanan",
            btnFilterLainnya to "Lainnya"
        )

        for ((button, category) in buttons) {
            val isSelected = category == currentCategory
            button.setBackgroundResource(
                if (isSelected) R.drawable.bg_pill_navy else R.drawable.bg_pill_outline_navy
            )
            button.setTextColor(
                if (isSelected) resources.getColor(android.R.color.white, null)
                else android.graphics.Color.parseColor("#0F2A4D")
            )
        }
    }

    private fun setupDots(count: Int) {
        bannerCount = count
        currentBannerIndex = 0
        dotsContainer.removeAllViews()

        for (i in 0 until count) {
            val dot = View(requireContext())
            val size = if (i == 0) dpToPx(8) else dpToPx(6)
            val params = LinearLayout.LayoutParams(size, size)
            params.marginEnd = dpToPx(4)
            dot.layoutParams = params
            dot.setBackgroundResource(if (i == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
            dotsContainer.addView(dot)
        }
    }

    private fun updateDots() {
        for (i in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(i)
            val isActive = i == currentBannerIndex
            val size = dpToPx(if (isActive) 8 else 6)
            val params = dot.layoutParams
            params.width = size
            params.height = size
            dot.layoutParams = params
            dot.setBackgroundResource(if (isActive) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    companion object {
        // Banner statis/dekoratif, gambar official-artwork dari PokeAPI (sumber sama yang
        // udah dipakai di Pokedex & Starter Selection, resolusi bagus & bebas dipakai fan-project)
        private val FEATURED_BANNERS = listOf(
            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png",
                title = "Pikachu",
                subtitle = "Maskot Pokemon paling ikonik"
            ),
            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png",
                title = "Charizard",
                subtitle = "Evolusi akhir Charmander"
            ),
            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/150.png",
                title = "Mewtwo",
                subtitle = "Pokemon legendaris hasil rekayasa genetika"
            ),
            BannerDisplayItem(
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/133.png",
                title = "Eevee",
                subtitle = "Punya banyak jalur evolusi berbeda"
            )
        )
    }
}