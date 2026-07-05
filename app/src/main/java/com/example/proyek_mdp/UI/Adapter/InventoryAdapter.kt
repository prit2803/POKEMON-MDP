package com.example.proyek_mdp.UI.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Inventory.InventoryItem

class InventoryAdapter(
    private var list: List<InventoryItem>
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView =
            view.findViewById(R.id.imgItem)

        val name: TextView =
            view.findViewById(R.id.tvItemName)

        val quantity: TextView =
            view.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_inventory,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun getItemCount() =
        list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]

        holder.name.text = item.title

        holder.quantity.text =
            "Jumlah : x${item.quantity}"

        Glide.with(holder.itemView)
            .load(item.imagePath)
            .into(holder.image)
    }

    fun updateData(newData: List<InventoryItem>) {

        list = newData

        notifyDataSetChanged()
    }
}