package com.example.proyek_mdp.UI.Adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.Post
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter untuk card horizontal di Home ("Info Terbaru" & "Info Lainnya").
 * Klik card -> buka popup Shop, fokus ke post itu (di-handle di HomeFragment).
 */
class HomeInfoAdapter(
    private val onItemClick: (Post) -> Unit
) : ListAdapter<Post, HomeInfoAdapter.InfoViewHolder>(DiffCallback()) {

    class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivInfoImage)
        val tvTitle: TextView = view.findViewById(R.id.tvInfoTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvInfoPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_info, parent, false)
        return InfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        val post = getItem(position)
        val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.tvTitle.text = post.title
        holder.tvPrice.text = rupiah.format(post.price)

        if (!post.imagePath.isNullOrEmpty()) {
            holder.ivImage.setImageBitmap(BitmapFactory.decodeFile(post.imagePath))
        } else {
            holder.ivImage.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener { onItemClick(post) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}