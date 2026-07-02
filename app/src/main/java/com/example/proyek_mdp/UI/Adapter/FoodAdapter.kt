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
    private val foodList: List<Food>,
    private val onBuyClick: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvFoodEmoji)
        val tvName: TextView = view.findViewById(R.id.tvFoodName)
        val tvDescription: TextView = view.findViewById(R.id.tvFoodDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvFoodPrice)
        val btnBuy: Button = view.findViewById(R.id.btnBuyFood)
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
        holder.btnBuy.setOnClickListener { onBuyClick(food) }
    }

    override fun getItemCount(): Int = foodList.size
}