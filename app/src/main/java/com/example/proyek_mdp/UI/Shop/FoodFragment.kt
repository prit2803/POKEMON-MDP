package com.example.proyek_mdp.UI.Shop

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class FoodFragment : Fragment(R.layout.fragment_shop) {

    private lateinit var tvCoinBalance: TextView
    private lateinit var tvStreakInfo: TextView
    private lateinit var btnClaimDaily: Button
    private lateinit var rvFoodList: RecyclerView

    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

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
                rvFoodList.adapter = FoodAdapter(foods) { food ->
                    handleBuyFood(food)
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

        if (user.coins < food.price) {
            Toast.makeText(requireContext(), "Koin kamu tidak cukup", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())

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