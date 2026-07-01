package com.example.proyek_mdp.admin

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

class PostAdapter(
    private val onItemLongClick: (Post) -> Unit = {}
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivPostImage)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.tvTitle.text = post.title
        holder.tvDescription.text = post.description
        holder.tvCategory.text = post.category
        holder.tvPrice.text = rupiah.format(post.price)

        if (!post.imagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(post.imagePath)
            holder.ivImage.setImageBitmap(bitmap)
        } else {
            holder.ivImage.setImageDrawable(null)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(post)
            true
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}