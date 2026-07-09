package com.example.proyek_mdp.UI.Adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.Data.local.entity.Post
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter khusus popup Shop. Bedanya dari PostAdapter (Feed/Admin):
 * ada tombol Beli + info stok, dan bisa nge-highlight 1 item
 * (dipakai saat dibuka dari Feed lewat klik promo).
 */
class ShopAdapter(
    private val highlightPostId: Int,
    private val onBuyClick: (Post) -> Unit
) : ListAdapter<Post, ShopAdapter.ShopViewHolder>(DiffCallback()) {

    class ShopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivShopImage)
        val tvTitle: TextView = view.findViewById(R.id.tvShopTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvShopDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvShopPrice)
        val tvStock: TextView = view.findViewById(R.id.tvShopStock)
        val btnBuy: Button = view.findViewById(R.id.btnBuyPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop_post, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val post = getItem(position)
        val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.tvTitle.text = post.title
        holder.tvDescription.text = post.description
        holder.tvPrice.text = rupiah.format(post.price)
        holder.tvStock.text = "Stok: ${post.stock}"

        if (!post.imagePath.isNullOrEmpty()) {
            holder.ivImage.setImageBitmap(BitmapFactory.decodeFile(post.imagePath))
        } else {
            holder.ivImage.setImageDrawable(null)
        }

        if (post.stock <= 0) {
            holder.btnBuy.isEnabled = false
            holder.btnBuy.text = "Habis"
        } else {
            holder.btnBuy.isEnabled = true
            holder.btnBuy.text = "Beli"
        }
        holder.btnBuy.setOnClickListener { onBuyClick(post) }

        // Item yang dibuka dari klik promo di Feed dikasih warna beda sebentar
        holder.itemView.setBackgroundColor(
            if (post.id == highlightPostId) Color.parseColor("#FFF9C4") else Color.WHITE
        )
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}