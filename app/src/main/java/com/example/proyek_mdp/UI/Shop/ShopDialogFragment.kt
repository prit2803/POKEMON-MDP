package com.example.proyek_mdp.UI.Shop

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.ShopAdapter
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.Data.local.database.AppDatabase
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.local.entity.PurchaseHistory
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.Data.local.entity.UserInventory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Popup Shop. Isinya diambil langsung dari Post (postDao.getActivePosts()) yang
 * dibuat admin — jadi item shop selalu sama dengan yang admin post, bukan katalog tetap.
 *
 * Dipanggil dari HomeFragment (tanpa highlight):
 *   ShopDialogFragment.newInstance().show(childFragmentManager, "shop")
 *
 * Dipanggil dari FeedFragment (klik promo, langsung fokus ke item itu):
 *   ShopDialogFragment.newInstance(post.id).show(childFragmentManager, "shop")
 *
 * Pakai Flow, jadi stok ke-update REAL-TIME otomatis ke semua yang lagi buka
 * popup ini, tanpa perlu refresh manual, karena Room otomatis re-emit query
 * setiap tabel posts berubah.
 */
class ShopDialogFragment : DialogFragment(R.layout.fragment_shop) {

    private lateinit var tvCoinBalance: TextView
    private lateinit var tvStreakInfo: TextView
    private lateinit var btnClaimDaily: Button
    private lateinit var rvShop: RecyclerView

    private lateinit var sessionManager: SessionManager
    private lateinit var shopAdapter: ShopAdapter
    private var currentUser: User? = null

    private var highlightPostId: Int = -1
    private var hasScrolledToHighlight = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        highlightPostId = arguments?.getInt(ARG_HIGHLIGHT_POST_ID, -1) ?: -1
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        tvCoinBalance = view.findViewById(R.id.tvCoinBalance)
        tvStreakInfo = view.findViewById(R.id.tvStreakInfo)
        btnClaimDaily = view.findViewById(R.id.btnClaimDaily)
        rvShop = view.findViewById(R.id.rvFoodList) // id lama di fragment_shop.xml, tetap dipakai

        shopAdapter = ShopAdapter(highlightPostId) { post -> handleBuy(post) }
        rvShop.layoutManager = LinearLayoutManager(requireContext())
        rvShop.adapter = shopAdapter

        btnClaimDaily.setOnClickListener { handleClaimDaily() }

        loadUser()
        observePosts()
    }
    override fun onResume() {
        super.onResume()
        loadUser()
    }

    private fun loadUser() {

        val userId = sessionManager.getUserId()

        if (userId == -1) return

        viewLifecycleOwner.lifecycleScope.launch {

            val db = AppDatabase.getDatabase(requireContext())

            currentUser = db.userDao().getUserById(userId)

            if (!isAdded) return@launch

            updateCoinDisplay()

            // refresh adapter supaya callback beli memakai currentUser terbaru
            shopAdapter.notifyDataSetChanged()

        }

    }
    private fun observePosts() {
        val db = AppDatabase.getDatabase(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            db.postDao().getActivePosts().collect { posts ->
                if (!isAdded) return@collect

                shopAdapter.submitList(posts) {
                    // Dijalankan setelah RecyclerView selesai update, biar posisi index-nya valid
                    if (!hasScrolledToHighlight && highlightPostId != -1) {
                        val index = posts.indexOfFirst { it.id == highlightPostId }
                        if (index != -1) {
                            rvShop.scrollToPosition(index)
                            hasScrolledToHighlight = true
                        }
                    }
                }
            }
        }
    }

    private fun updateCoinDisplay() {
        val user = currentUser ?: return
        tvCoinBalance.text = "Koin kamu: ${user.coins} \uD83E\uDE99"
        tvStreakInfo.text = "Streak login: ${user.streakCount} hari"
    }

    private fun handleClaimDaily() {
        val user = currentUser ?: return
        val today = getTodayString()
        val last = user.lastClaimDate

        if (last == today) {
            Toast.makeText(requireContext(), "Kamu sudah klaim hari ini, balik lagi besok ya!", Toast.LENGTH_SHORT).show()
            return
        }

        val newStreak = if (last != null && daysBetween(last, today) == 1L) {
            user.streakCount + 1
        } else {
            1
        }

        val reward = 10 + (newStreak - 1) * 5

        user.coins += reward
        user.streakCount = newStreak
        user.lastClaimDate = today

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.userDao().update(user)

            if (isAdded) {
                updateCoinDisplay()
                Toast.makeText(
                    requireContext(),
                    "Klaim berhasil! +$reward koin (streak $newStreak hari)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleBuy(post: Post) {
        val user = currentUser ?: return

        if (post.stock <= 0) {
            Toast.makeText(requireContext(), "Stok ${post.title} sudah habis", Toast.LENGTH_SHORT).show()
            return
        }

        if (user.coins < post.price) {

            CoinNotEnoughDialog(

                user.coins,

                post.price.toInt()

            ).show(

                childFragmentManager,

                "coin_dialog"

            )

            return

        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            // Kurangi stok global dulu, dicek atomik di level SQL (aman kalau ada user lain beli bersamaan)
            val rowsUpdated = db.postDao().decreaseStock(post.id)

            if (rowsUpdated == 0) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Stok ${post.title} baru saja habis", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            user.coins -= post.price.toInt()
            db.userDao().update(user)
            db.purchaseHistoryDao().insert(

                PurchaseHistory(

                    userId = user.id,

                    postId = post.id,

                    itemName = post.title,

                    price = post.price.toInt(),

                    quantity = 1

                )

            )
            currentUser = db.userDao().getUserById(user.id)

            val existing = db.userInventoryDao().getItem(user.id, post.id)

            if (existing != null) {
                existing.quantity += 1
                db.userInventoryDao().update(existing)
            } else {
                db.userInventoryDao().insert(
                    UserInventory(
                        userId = user.id,
                        postId = post.id,
                        quantity = 1
                    )
                )
            }

            if (isAdded) {
                updateCoinDisplay()
                Toast.makeText(requireContext(), "Berhasil beli ${post.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun daysBetween(dateStr1: String, dateStr2: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d1 = sdf.parse(dateStr1) ?: return -1
        val d2 = sdf.parse(dateStr2) ?: return -1
        val diff = d2.time - d1.time
        return diff / (1000 * 60 * 60 * 24)
    }

    companion object {
        private const val ARG_HIGHLIGHT_POST_ID = "highlight_post_id"

        fun newInstance(highlightPostId: Int? = null): ShopDialogFragment {
            val fragment = ShopDialogFragment()
            if (highlightPostId != null) {
                fragment.arguments = Bundle().apply {
                    putInt(ARG_HIGHLIGHT_POST_ID, highlightPostId)
                }
            }
            return fragment
        }
    }
}