package com.example.proyek_mdp.UI.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.Food

class FoodAdapter(
    private var foodList: List<Food>,
    private val onBuyClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvFoodEmoji)
        val tvName: TextView = view.findViewById(R.id.tvFoodName)
        val tvDescription: TextView = view.findViewById(R.id.tvFoodDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvFoodPrice)
        val tvStock: TextView = view.findViewById(R.id.tvFoodStock)
        val btnBuy: Button = view.findViewById(R.id.btnBuyFood)
    }

    /** Dipanggil setelah stok berubah (habis beli / refresh), supaya list ke-update tanpa bikin adapter baru. */
    fun updateData(newList: List<Food>) {
        foodList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.tvEmoji.text = food.emoji
        holder.tvName.text = food.name
        holder.tvDescription.text = food.description
        holder.tvPrice.text = "${food.price} koin"
        holder.tvStock.text = "Stok: ${food.stock}"

        if (food.stock <= 0) {
            holder.btnBuy.isEnabled = false
            holder.btnBuy.text = "Habis"
        } else {
            holder.btnBuy.isEnabled = true
            holder.btnBuy.text = "Beli"
        }

        holder.btnBuy.setOnClickListener { onBuyClick(food) }
    }

    override fun getItemCount(): Int = foodList.size
}