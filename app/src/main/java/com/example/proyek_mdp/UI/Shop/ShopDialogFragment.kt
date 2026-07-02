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
import com.example.proyek_mdp.UI.Adapter.FoodAdapter
import com.example.proyek_mdp.auth.SessionManager
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.Food
import com.example.proyek_mdp.database.User
import com.example.proyek_mdp.database.UserFood
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Popup shop makanan. Dipanggil dari HomeFragment lewat:
 *   ShopDialogFragment().show(childFragmentManager, "shop")
 *
 * Stok makanan bersifat GLOBAL (bukan per-user) — kalau user lain beli duluan,
 * stok berkurang untuk semua orang. Dicek ulang di level database (decreaseStock)
 * supaya tidak bisa minus walau ada beberapa user beli hampir bersamaan.
 */
class ShopDialogFragment : DialogFragment(R.layout.fragment_shop) {

    private lateinit var tvCoinBalance: TextView
    private lateinit var tvStreakInfo: TextView
    private lateinit var btnClaimDaily: Button
    private lateinit var rvFoodList: RecyclerView

    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null
    private var foodAdapter: FoodAdapter? = null

    override fun onStart() {
        super.onStart()
        // Bikin dialog selebar layar, tinggi menyesuaikan isi, background transparan
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
        rvFoodList = view.findViewById(R.id.rvFoodList)

        rvFoodList.layoutManager = LinearLayoutManager(requireContext())

        btnClaimDaily.setOnClickListener {
            handleClaimDaily()
        }

        loadData()
    }

    private fun loadData() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            val user = db.userDao().getUserById(userId)
            val foods = db.foodDao().getAllFood()

            currentUser = user

            if (isAdded) {
                updateCoinDisplay()
                foodAdapter = FoodAdapter(foods) { food -> handleBuyFood(food) }
                rvFoodList.adapter = foodAdapter
            }
        }
    }

    private fun refreshFoodList() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val foods = db.foodDao().getAllFood()
            if (isAdded) {
                foodAdapter?.updateData(foods)
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

        lifecycleScope.launch {
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

    private fun handleBuyFood(food: Food) {
        val user = currentUser ?: return

        if (food.stock <= 0) {
            Toast.makeText(requireContext(), "Stok ${food.name} sudah habis", Toast.LENGTH_SHORT).show()
            return
        }

        if (user.coins < food.price) {
            Toast.makeText(requireContext(), "Koin kamu tidak cukup", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

            // Kurangi stok global dulu, dicek atomik di level SQL (aman kalau ada user lain beli bersamaan)
            val rowsUpdated = db.foodDao().decreaseStock(food.id)

            if (rowsUpdated == 0) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Stok ${food.name} baru saja habis", Toast.LENGTH_SHORT).show()
                    refreshFoodList()
                }
                return@launch
            }

            user.coins -= food.price
            db.userDao().update(user)

            val existing = db.userFoodDao().getUserFoodItem(user.id, food.id)
            if (existing != null) {
                existing.quantity += 1
                db.userFoodDao().update(existing)
            } else {
                db.userFoodDao().insert(UserFood(userId = user.id, foodId = food.id, quantity = 1))
            }

            if (isAdded) {
                updateCoinDisplay()
                refreshFoodList()
                Toast.makeText(requireContext(), "Berhasil beli ${food.name}", Toast.LENGTH_SHORT).show()
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
}